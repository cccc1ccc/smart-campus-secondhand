package com.must.secondhand.config;

import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.entity.ListingStatusHistory;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ItemCondition;
import com.must.secondhand.model.enums.ListingStatus;
import com.must.secondhand.model.enums.RequestStatus;
import com.must.secondhand.repository.BuyRequestRepository;
import com.must.secondhand.repository.ListingRepository;
import com.must.secondhand.repository.ListingStatusHistoryRepository;
import com.must.secondhand.repository.UserRepository;
import com.must.secondhand.service.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Loads demo data on an empty database: eight MUST students, a curated demo scenario that
 * produces real matches, and 5,000 listings so the search benchmark of the URS can be run.
 * Bulk listings are backdated, so the scheduled retry does not process them again.
 */
@Component
public class SeedDataRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);
    private static final String DEMO_PASSWORD = "Password123";

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final ListingStatusHistoryRepository statusHistoryRepository;
    private final MatchingService matchingService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.listings:5000}")
    private int targetListings;

    public SeedDataRunner(UserRepository userRepository,
                          ListingRepository listingRepository,
                          BuyRequestRepository buyRequestRepository,
                          ListingStatusHistoryRepository statusHistoryRepository,
                          MatchingService matchingService,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.buyRequestRepository = buyRequestRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.matchingService = matchingService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Seed data disabled (app.seed.enabled=false)");
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Database already contains data, seed skipped");
            return;
        }

        Instant start = Instant.now();
        List<User> students = createStudents();
        createCuratedListings(students);
        List<BuyRequest> requests = createRequests(students);

        int curated = (int) listingRepository.count();
        int bulk = Math.max(0, targetListings - curated);
        // Bulk data belongs to the "other students" so the demo accounts stay under the
        // 50-active-listing limit and remain usable for the live demonstration.
        createBulkListings(students.subList(8, students.size()), bulk);

        log.info("Seed finished in {} ms: {} students, {} listings ({} curated), {} active requests",
                Duration.between(start, Instant.now()).toMillis(),
                students.size(), listingRepository.count(), curated, requests.size());
        log.info("Demo login -> student number 1240016186, password {}", DEMO_PASSWORD);
    }

    // ---------------------------------------------------------------- students

    private List<User> createStudents() {
        String[][] rows = {
                {"Wu Yutong", "1240016186", "wu.yutong@student.must.edu.mo"},
                {"Wang Yike", "1240012305", "wang.yike@student.must.edu.mo"},
                {"Chen Moxiong", "1240008865", "chen.moxiong@student.must.edu.mo"},
                {"Gao Shengzhe", "1240008949", "gao.shengzhe@student.must.edu.mo"},
                {"Ao Iek Hou", "1240010111", "ao.iekhou@student.must.edu.mo"},
                {"Chan Ka Ian", "1240011222", "chan.kaian@student.must.edu.mo"},
                {"Lei Chi Fong", "1240012333", "lei.chifong@student.must.edu.mo"},
                {"Ho Weng Ian", "1240013444", "ho.wengian@student.must.edu.mo"},
                {"Lam Chi Wai", "1240014501", "lam.chiwai@student.must.edu.mo"},
                {"Wong Mei Ling", "1240014502", "wong.meiling@student.must.edu.mo"},
                {"Cheong Hou In", "1240014503", "cheong.houin@student.must.edu.mo"},
                {"Fong Ka Kit", "1240014504", "fong.kakit@student.must.edu.mo"},
                {"Ieong Sin Hang", "1240014505", "ieong.sinhang@student.must.edu.mo"},
                {"Tam Wai Lok", "1240014506", "tam.wailok@student.must.edu.mo"},
                {"Kwok Hoi Ying", "1240014507", "kwok.hoiying@student.must.edu.mo"},
                {"Ng Chi Hang", "1240014508", "ng.chihang@student.must.edu.mo"}
        };
        List<User> students = new ArrayList<>();
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        for (String[] row : rows) {
            User user = new User(row[0], row[1], row[2], hash);
            user.setContact("+853 6" + row[1].substring(row[1].length() - 7));
            user.setCreatedAt(Instant.now());
            students.add(userRepository.save(user));
        }
        return students;
    }

    // ------------------------------------------------------------ demo listings

    private record SeedListing(int sellerIndex, String title, Category category, ItemCondition condition,
                               String price, String description) {
    }

    private void createCuratedListings(List<User> students) {
        List<SeedListing> seeds = List.of(
                new SeedListing(0, "24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS, ItemCondition.LIKE_NEW,
                        "800.00", "Bought last year for my dorm and upgraded to a 27-inch. No dead pixels, HDMI cable included."),
                new SeedListing(0, "Logitech MX Master 3 Wireless Mouse", Category.ELECTRONICS, ItemCondition.GOOD,
                        "320.00", "Used for one semester. Scroll wheel works perfectly, USB receiver included."),
                new SeedListing(0, "Introduction to Algorithms 3rd Edition (CLRS)", Category.BOOKS, ItemCondition.GOOD,
                        "260.00", "CLRS hardcover, no missing pages, a few pencil notes in chapter 4."),
                new SeedListing(0, "IKEA Linnmon Desk 100x60cm", Category.FURNITURE, ItemCondition.GOOD,
                        "180.00", "White desk top with four legs. Small scratch on the back edge, pick up at Block R."),
                new SeedListing(0, "Anker 65W USB-C Charger", Category.ELECTRONICS, ItemCondition.LIKE_NEW,
                        "90.00", "GaN charger with one USB-C and one USB-A port, original box included."),
                new SeedListing(0, "Sony WH-1000XM4 Headphones", Category.ELECTRONICS, ItemCondition.GOOD,
                        "950.00", "Noise cancelling headphones. Ear pads replaced last month, carrying case included."),
                new SeedListing(1, "Calculus: Early Transcendentals 8th Edition", Category.BOOKS, ItemCondition.GOOD,
                        "240.00", "Stewart 8th edition, used for MATH101, no torn pages."),
                new SeedListing(1, "iPad Air 4 64GB Wi-Fi", Category.ELECTRONICS, ItemCondition.GOOD,
                        "2200.00", "Space grey, screen protector since day one, charger included."),
                new SeedListing(1, "MUST Hoodie Size L", Category.CLOTHING, ItemCondition.LIKE_NEW,
                        "120.00", "Official MUST hoodie, navy blue, worn twice."),
                new SeedListing(2, "Badminton Racket Yonex Astrox 88D", Category.SPORTS, ItemCondition.GOOD,
                        "480.00", "Restrung last month at 26 lbs, grip replaced."),
                new SeedListing(2, "Mini Rice Cooker 1.2L", Category.DAILY_SUPPLIES, ItemCondition.LIKE_NEW,
                        "150.00", "Perfect size for one person, used for three months."),
                new SeedListing(3, "Office Chair with Lumbar Support", Category.FURNITURE, ItemCondition.GOOD,
                        "350.00", "Mesh back chair, adjustable height, wheels work fine."),
                new SeedListing(3, "Scientific Calculator TI-84 Plus CE", Category.OTHER, ItemCondition.GOOD,
                        "380.00", "Used for calculus and physics, batteries included."),
                new SeedListing(4, "Mechanical Keyboard 87 Keys Brown Switch", Category.ELECTRONICS, ItemCondition.LIKE_NEW,
                        "260.00", "Tenkeyless layout, PBT keycaps, USB-C detachable cable."),
                new SeedListing(5, "Yoga Mat 6mm", Category.SPORTS, ItemCondition.GOOD,
                        "60.00", "Non-slip mat, cleaned after every use."),
                new SeedListing(6, "Bedside Table White", Category.FURNITURE, ItemCondition.GOOD,
                        "90.00", "Two drawers, small chip at the bottom corner."),
                new SeedListing(1, "Kindle Paperwhite 11th Gen", Category.ELECTRONICS, ItemCondition.GOOD,
                        "620.00", "8GB model with a case, battery still holds a full week."),
                new SeedListing(2, "Desk Fan USB Powered", Category.DAILY_SUPPLIES, ItemCondition.GOOD,
                        "45.00", "Three speeds, quiet at the lowest setting."));

        for (SeedListing seed : seeds) {
            Listing listing = new Listing();
            listing.setSeller(students.get(seed.sellerIndex()));
            listing.setTitle(seed.title());
            listing.setCategory(seed.category());
            listing.setDescription(seed.description());
            listing.setCondition(seed.condition());
            listing.setPrice(new BigDecimal(seed.price()));
            listing.setStatus(ListingStatus.AVAILABLE);
            Instant createdAt = Instant.now().minus(Duration.ofHours(seeds.indexOf(seed)));
            listing.setCreatedAt(createdAt);
            listing.setUpdatedAt(createdAt);
            listing = listingRepository.save(listing);
            statusHistoryRepository.save(new ListingStatusHistory(listing, null, ListingStatus.AVAILABLE,
                    listing.getSeller().getId()));
        }

        // Two extra listings that demonstrate the Reserved and Sold states.
        List<Listing> all = listingRepository.findBySellerIdOrderByCreatedAtDesc(students.get(1).getId());
        Listing kindle = all.stream().filter(item -> item.getTitle().startsWith("Kindle")).findFirst().orElse(null);
        if (kindle != null) {
            moveTo(kindle, ListingStatus.SOLD);
        }
        List<Listing> seller2 = listingRepository.findBySellerIdOrderByCreatedAtDesc(students.get(2).getId());
        Listing fan = seller2.stream().filter(item -> item.getTitle().startsWith("Desk Fan")).findFirst().orElse(null);
        if (fan != null) {
            moveTo(fan, ListingStatus.RESERVED);
        }
    }

    private void moveTo(Listing listing, ListingStatus status) {
        ListingStatus previous = listing.getStatus();
        listing.setStatus(status);
        listing.setUpdatedAt(Instant.now());
        if (status == ListingStatus.SOLD) {
            listing.setSoldAt(Instant.now());
        }
        listingRepository.save(listing);
        statusHistoryRepository.save(new ListingStatusHistory(listing, previous, status,
                listing.getSeller().getId()));
    }

    // ------------------------------------------------------------ demo requests

    private record SeedRequest(int buyerIndex, String keywords, Category category, String min, String max,
                               ItemCondition acceptable, String requirements) {
    }

    private List<BuyRequest> createRequests(List<User> students) {
        List<SeedRequest> seeds = List.of(
                new SeedRequest(2, "monitor 24 inch hdmi ips", Category.ELECTRONICS, "600", "900",
                        ItemCondition.GOOD, "Prefer an IPS panel, no dead pixels."),
                new SeedRequest(3, "algorithm textbook clrs", Category.BOOKS, "150", "300",
                        ItemCondition.GOOD, "Needed for the algorithms course this term."),
                new SeedRequest(1, "mouse wireless logitech", Category.ELECTRONICS, "200", "400",
                        ItemCondition.GOOD, "Must include the USB receiver."),
                new SeedRequest(4, "desk ikea white", Category.FURNITURE, "100", "250",
                        ItemCondition.GOOD, "Needs to fit a 100 cm wide space."),
                new SeedRequest(5, "headphones noise cancelling sony", Category.ELECTRONICS, "700", "1100",
                        ItemCondition.GOOD, "Over-ear preferred, carrying case needed."),
                new SeedRequest(6, "charger usb c 65w", Category.ELECTRONICS, "50", "120",
                        ItemCondition.LIKE_NEW, "Must support PD charging for my laptop."),
                new SeedRequest(7, "rice cooker small", Category.DAILY_SUPPLIES, "100", "200",
                        ItemCondition.LIKE_NEW, "One person portion is enough."),
                new SeedRequest(3, "calculator ti-84", Category.OTHER, "250", "450",
                        ItemCondition.GOOD, "Needed before the final exam."));

        List<BuyRequest> created = new ArrayList<>();
        for (SeedRequest seed : seeds) {
            BuyRequest request = new BuyRequest();
            request.setBuyer(students.get(seed.buyerIndex()));
            request.setKeywords(seed.keywords());
            request.setCategory(seed.category());
            request.setMinBudget(new BigDecimal(seed.min()));
            request.setMaxBudget(new BigDecimal(seed.max()));
            request.setAcceptableCondition(seed.acceptable());
            request.setRequirements(seed.requirements());
            request.setStatus(RequestStatus.ACTIVE);
            request.setCreatedAt(Instant.now());
            request.setUpdatedAt(Instant.now());
            request = buyRequestRepository.save(request);
            matchingService.runForRequest(request);
            created.add(request);
        }
        return created;
    }

    // -------------------------------------------------------------- bulk data

    private static final String[][] TITLES = {
            // BOOKS
            {"Calculus: Early Transcendentals 8th Edition", "Linear Algebra Done Right 4th Edition",
                    "Introduction to Algorithms 3rd Edition", "Discrete Mathematics and Its Applications",
                    "Principles of Economics Mankiw", "Organic Chemistry Wade 9th Edition",
                    "Probability and Statistics for Engineers", "Clean Code by Robert Martin",
                    "Database System Concepts 7th Edition", "Operating System Concepts 10th Edition"},
            // ELECTRONICS
            {"24-inch IPS Monitor 75Hz HDMI", "Logitech MX Master 3 Wireless Mouse", "iPad Air 4 64GB Wi-Fi",
                    "Kindle Paperwhite 11th Gen", "Anker 65W USB-C Charger", "Sony WH-1000XM4 Headphones",
                    "Mechanical Keyboard 87 Keys Brown Switch", "Xiaomi Power Bank 20000mAh",
                    "USB-C Hub 7-in-1 HDMI", "Raspberry Pi 4 Model B 4GB", "Dell 27-inch 4K Monitor",
                    "AirPods Pro 1st Gen", "ThinkPad T480 Laptop i5", "Canon EOS M50 Camera Body",
                    "TP-Link WiFi Router AX1800", "JBL Flip 5 Bluetooth Speaker",
                    "Graphics Card GTX 1660 Super", "Wacom Drawing Tablet CTL-472"},
            // FURNITURE
            {"IKEA Linnmon Desk 100x60cm", "Office Chair with Lumbar Support", "Bedside Table White",
                    "Bookshelf 4-tier", "Floor Lamp Warm Light", "Folding Study Table",
                    "Single Bed Frame Metal", "Shoe Rack 3-tier", "IKEA Markus Chair",
                    "Storage Cabinet with Doors"},
            // DAILY_SUPPLIES
            {"Electric Kettle 1.5L", "Mini Rice Cooker 1.2L", "Desk Fan USB Powered",
                    "Storage Box Set of 3", "Single Bed Sheet Set", "LED Desk Lamp Touch Dimming",
                    "Electric Toothbrush", "Hair Dryer 1800W", "Foldable Laundry Basket",
                    "Non-stick Cookware Set"},
            // CLOTHING
            {"Uniqlo Down Jacket Size M", "MUST Hoodie Size L", "Running Shoes Size 42",
                    "Leather Backpack", "Wool Coat Size M", "Slim Jeans Size 30",
                    "Windproof Sports Jacket", "Canvas Sneakers Size 41"},
            // SPORTS
            {"Badminton Racket Yonex Astrox", "Yoga Mat 6mm", "Dumbbell Set 2x5kg",
                    "Basketball Spalding", "Tennis Racket Wilson", "Adjustable Skipping Rope",
                    "Speedo Swimming Goggles", "Table Tennis Paddle Set"},
            // OTHER
            {"Scientific Calculator TI-84 Plus CE", "Bamboo Desk Organizer", "Bicycle 26 inch",
                    "Yamaha F310 Guitar", "Violin 4/4 Student Model", "Board Game Catan",
                    "Sewing Kit Basic", "Ceramic Plant Pot Set", "Automatic Umbrella", "Tool Kit 38 Pieces"}
    };

    private static final String[] VARIANTS = {
            "", " - second owner", " with case", " (2021)", " bundle", " spare unit",
            " - minor wear", " like new condition"};

    private static final String[] NOTES = {
            "Works well, no functional issue", "Minor wear on the surface",
            "Kept clean and dry in the dorm", "Upgraded, so it is spare now",
            "All accessories included as described"};

    private static final String[] PLACES = {
            "Block A dorm", "Block R dorm", "Library entrance", "Student Centre", "W1 canteen"};

    private static final String[] PERIODS = {"one semester", "two semesters", "one year", "a few months"};

    private void createBulkListings(List<User> students, int count) {
        if (count <= 0) {
            return;
        }
        Random random = new Random(20261105L);
        String sql = """
                insert into listings
                (seller_id, title, category, description, item_condition, price, status, photo_url, created_at, updated_at, sold_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        List<Object[]> batch = new ArrayList<>(count);
        Instant now = Instant.now();

        for (int i = 0; i < count; i++) {
            int categoryIndex = random.nextInt(TITLES.length);
            String[] pool = TITLES[categoryIndex];
            String title = pool[random.nextInt(pool.length)] + VARIANTS[random.nextInt(VARIANTS.length)];
            if (title.length() > 100) {
                title = title.substring(0, 100);
            }
            Category category = Category.values()[categoryIndex];
            ItemCondition condition = ItemCondition.values()[random.nextInt(ItemCondition.values().length)];
            BigDecimal price = randomPrice(random, category, condition, title);
            String description = "Used for " + PERIODS[random.nextInt(PERIODS.length)] + " at MUST. "
                    + NOTES[random.nextInt(NOTES.length)] + ". Pick up at " + PLACES[random.nextInt(PLACES.length)] + ".";

            ListingStatus status = ListingStatus.AVAILABLE;
            int dice = random.nextInt(100);
            if (dice < 3) {
                status = ListingStatus.SOLD;
            } else if (dice < 7) {
                status = ListingStatus.RESERVED;
            }

            // Backdated by 2 to 45 days so the scheduled retry does not re-process them.
            Instant createdAt = now.minus(Duration.ofMinutes(2L * 24 * 60 + random.nextInt(43 * 24 * 60)));
            Timestamp created = Timestamp.from(createdAt);
            Timestamp soldAt = status == ListingStatus.SOLD ? created : null;

            batch.add(new Object[]{
                    students.get(random.nextInt(students.size())).getId(),
                    title,
                    category.name(),
                    description,
                    condition.name(),
                    price,
                    status.name(),
                    null,
                    created,
                    created,
                    soldAt
            });
        }
        jdbcTemplate.batchUpdate(sql, batch);
        log.info("Inserted {} bulk listings", count);
    }

    private BigDecimal randomPrice(Random random, Category category, ItemCondition condition, String title) {
        boolean premium = title.toLowerCase().matches(".*(laptop|ipad|monitor|camera|graphics|tablet|airpods|thinkpad|raspberry).*");
        int base = switch (category) {
            case BOOKS -> 80 + random.nextInt(320);
            case ELECTRONICS -> premium ? 700 + random.nextInt(1800) : 60 + random.nextInt(500);
            case FURNITURE -> 60 + random.nextInt(500);
            case DAILY_SUPPLIES -> 30 + random.nextInt(260);
            case CLOTHING -> 50 + random.nextInt(400);
            case SPORTS -> 40 + random.nextInt(500);
            case OTHER -> 40 + random.nextInt(400);
        };
        // Better condition keeps a higher share of the base price.
        double factor = switch (condition) {
            case NEW -> 1.0;
            case LIKE_NEW -> 0.85;
            case GOOD -> 0.7;
            case FAIR -> 0.5;
        };
        double price = Math.max(20, base * factor);
        return BigDecimal.valueOf(Math.round(price * 100) / 100.0).setScale(2);
    }
}
