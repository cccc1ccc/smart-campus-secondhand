package com.must.secondhand.service;

import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ItemCondition;
import com.must.secondhand.model.enums.ListingStatus;
import com.must.secondhand.model.enums.RequestStatus;
import com.must.secondhand.repository.BuyRequestRepository;
import com.must.secondhand.repository.ListingRepository;
import com.must.secondhand.repository.MatchRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests of the matching rule documented in REQ-5.1. */
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private BuyRequestRepository buyRequestRepository;
    @Mock
    private MatchRecordRepository matchRecordRepository;
    @Mock
    private NotificationService notificationService;

    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(listingRepository, buyRequestRepository,
                matchRecordRepository, notificationService);
    }

    private Listing listing(String title, Category category, ItemCondition condition, String price,
                            ListingStatus status) {
        User seller = new User("Seller", "1240000001", "seller@student.must.edu.mo", "hash");
        seller.setId(1L);
        Listing listing = new Listing();
        listing.setId(10L);
        listing.setSeller(seller);
        listing.setTitle(title);
        listing.setDescription("Used one semester at MUST, pick up at Block R.");
        listing.setCategory(category);
        listing.setCondition(condition);
        listing.setPrice(new BigDecimal(price));
        listing.setStatus(status);
        return listing;
    }

    private BuyRequest request(String keywords, Category category, String min, String max,
                               ItemCondition acceptable) {
        User buyer = new User("Buyer", "1240000002", "buyer@student.must.edu.mo", "hash");
        buyer.setId(2L);
        BuyRequest request = new BuyRequest();
        request.setId(20L);
        request.setBuyer(buyer);
        request.setKeywords(keywords);
        request.setCategory(category);
        request.setMinBudget(new BigDecimal(min));
        request.setMaxBudget(new BigDecimal(max));
        request.setAcceptableCondition(acceptable);
        request.setStatus(RequestStatus.ACTIVE);
        return request;
    }

    @Test
    @DisplayName("A listing inside the budget with matching keywords is a match above 0.70")
    void qualifyFullMatch() {
        Listing listing = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.LIKE_NEW, "800", ListingStatus.AVAILABLE);
        BuyRequest request = request("monitor 24 inch hdmi ips", Category.ELECTRONICS, "600", "900",
                ItemCondition.GOOD);

        Optional<MatchingService.PairScore> score = matchingService.score(listing, request);

        assertThat(score).isPresent();
        assertThat(score.get().total()).isGreaterThan(MatchingService.SCORE_THRESHOLD);
        assertThat(score.get().category()).isEqualTo(1.0);
        assertThat(score.get().price()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Different categories never match")
    void rejectDifferentCategory() {
        Listing listing = listing("Yoga Mat 6mm", Category.SPORTS, ItemCondition.GOOD, "60",
                ListingStatus.AVAILABLE);
        BuyRequest request = request("monitor 24 inch hdmi", Category.ELECTRONICS, "600", "900",
                ItemCondition.GOOD);

        assertThat(matchingService.score(listing, request)).isEmpty();
    }

    @Test
    @DisplayName("A price above the budget is rejected")
    void rejectPriceAboveBudget() {
        Listing listing = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.GOOD, "1200", ListingStatus.AVAILABLE);
        BuyRequest request = request("monitor 24 inch hdmi", Category.ELECTRONICS, "600", "900",
                ItemCondition.GOOD);

        assertThat(matchingService.score(listing, request)).isEmpty();
    }

    @Test
    @DisplayName("A price below the budget is rejected")
    void rejectPriceBelowBudget() {
        Listing listing = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.GOOD, "300", ListingStatus.AVAILABLE);
        BuyRequest request = request("monitor 24 inch hdmi", Category.ELECTRONICS, "600", "900",
                ItemCondition.GOOD);

        assertThat(matchingService.score(listing, request)).isEmpty();
    }

    @Test
    @DisplayName("A condition worse than the buyer accepts is rejected")
    void rejectWorseCondition() {
        Listing listing = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.FAIR, "800", ListingStatus.AVAILABLE);
        BuyRequest request = request("monitor 24 inch hdmi", Category.ELECTRONICS, "600", "900",
                ItemCondition.LIKE_NEW);

        assertThat(matchingService.score(listing, request)).isEmpty();
    }

    @Test
    @DisplayName("Weak keyword similarity is rejected even when category and price fit")
    void rejectWeakKeywords() {
        Listing listing = listing("Electric Kettle 1.5L", Category.ELECTRONICS,
                ItemCondition.GOOD, "800", ListingStatus.AVAILABLE);
        BuyRequest request = request("monitor 24 inch hdmi ips", Category.ELECTRONICS, "600", "900",
                ItemCondition.GOOD);

        assertThat(matchingService.score(listing, request)).isEmpty();
    }

    @Test
    @DisplayName("Reserved or sold listings are not matched")
    void rejectNonAvailableListing() {
        Listing listing = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.GOOD, "800", ListingStatus.SOLD);
        BuyRequest request = request("monitor 24 inch hdmi", Category.ELECTRONICS, "600", "900",
                ItemCondition.GOOD);

        assertThat(matchingService.score(listing, request)).isEmpty();
    }

    @Test
    @DisplayName("A better condition than required scores lower than an exact condition match")
    void conditionGapReducesScore() {
        BuyRequest request = request("monitor 24 inch hdmi", Category.ELECTRONICS, "600", "900",
                ItemCondition.FAIR);
        Listing exact = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.FAIR, "800", ListingStatus.AVAILABLE);
        Listing better = listing("24-inch IPS Monitor 75Hz HDMI", Category.ELECTRONICS,
                ItemCondition.NEW, "800", ListingStatus.AVAILABLE);

        double exactScore = matchingService.score(exact, request).orElseThrow().total();
        double betterScore = matchingService.score(better, request).orElseThrow().total();

        assertThat(exactScore).isGreaterThan(betterScore);
    }
}
