package com.must.secondhand.service;

import com.must.secondhand.common.BizException;
import com.must.secondhand.dto.ListingDetailView;
import com.must.secondhand.dto.ListingForm;
import com.must.secondhand.dto.ListingView;
import com.must.secondhand.dto.StatusHistoryView;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.entity.ListingStatusHistory;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.model.enums.ListingStatus;
import com.must.secondhand.model.enums.NotificationType;
import com.must.secondhand.repository.BuyRequestRepository;
import com.must.secondhand.repository.FavoriteRepository;
import com.must.secondhand.repository.ListingRepository;
import com.must.secondhand.repository.ListingStatusHistoryRepository;
import com.must.secondhand.repository.MatchRecordRepository;
import com.must.secondhand.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Publish, edit, delete and status management of listings (REQ-2.1, REQ-2.2, REQ-7.1). */
@Service
public class ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);
    private static final int MAX_ACTIVE_LISTINGS = 50;

    /** Allowed transitions in version 1; anything else is refused. */
    private static final Map<ListingStatus, Set<ListingStatus>> ALLOWED_TRANSITIONS = Map.of(
            ListingStatus.AVAILABLE, Set.of(ListingStatus.RESERVED),
            ListingStatus.RESERVED, Set.of(ListingStatus.SOLD, ListingStatus.AVAILABLE),
            ListingStatus.SOLD, Set.of());

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final ListingStatusHistoryRepository statusHistoryRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final MatchingService matchingService;
    private final NotificationService notificationService;

    public ListingService(ListingRepository listingRepository,
                          UserRepository userRepository,
                          FavoriteRepository favoriteRepository,
                          MatchRecordRepository matchRecordRepository,
                          ListingStatusHistoryRepository statusHistoryRepository,
                          BuyRequestRepository buyRequestRepository,
                          MatchingService matchingService,
                          NotificationService notificationService) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.buyRequestRepository = buyRequestRepository;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ListingView create(Long userId, ListingForm form) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> BizException.unauthorized("Session expired"));
        long active = listingRepository.countBySellerIdAndStatusIn(userId,
                List.of(ListingStatus.AVAILABLE, ListingStatus.RESERVED));
        if (active >= MAX_ACTIVE_LISTINGS) {
            throw new BizException("You already have " + MAX_ACTIVE_LISTINGS
                    + " active listings. Mark one as sold before publishing another.");
        }

        Listing listing = new Listing();
        listing.setSeller(seller);
        applyForm(listing, form);
        listing.setStatus(ListingStatus.AVAILABLE);
        Instant now = Instant.now();
        listing.setCreatedAt(now);
        listing.setUpdatedAt(now);
        listing = listingRepository.save(listing);

        statusHistoryRepository.save(new ListingStatusHistory(listing, null, ListingStatus.AVAILABLE, userId));
        triggerMatching(listing);
        return ListingView.of(listing, 0L);
    }

    @Transactional
    public ListingView update(Long userId, Long listingId, ListingForm form) {
        Listing listing = requireOwnedListing(userId, listingId);
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new BizException("Only available listings can be edited");
        }
        BigDecimal oldPrice = listing.getPrice();
        applyForm(listing, form);
        listing.setUpdatedAt(Instant.now());
        listing = listingRepository.save(listing);

        if (oldPrice.compareTo(listing.getPrice()) != 0) {
            notifyFavoritersAboutPrice(listing, oldPrice);
        }
        triggerMatching(listing);
        return ListingView.of(listing, favoriteRepository.countByListingId(listingId));
    }

    /**
     * Available -> Reserved -> Sold (and Reserved -> Available). The seller performs the
     * transition; matched buyers and users who favorited the listing are notified.
     */
    @Transactional
    public ListingView changeStatus(Long userId, Long listingId, ListingStatus target) {
        Listing listing = requireOwnedListing(userId, listingId);
        ListingStatus current = listing.getStatus();
        if (current == target) {
            throw new BizException("The listing is already " + target.name().toLowerCase());
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BizException("Status cannot change from " + label(current) + " to " + label(target) + " in version 1");
        }

        listing.setStatus(target);
        listing.setUpdatedAt(Instant.now());
        if (target == ListingStatus.SOLD) {
            listing.setSoldAt(Instant.now());
        }
        listing = listingRepository.save(listing);
        statusHistoryRepository.save(new ListingStatusHistory(listing, current, target, userId));

        if (target == ListingStatus.AVAILABLE) {
            // Back on the market: matching runs again, closed records may reopen.
            triggerMatching(listing);
        } else {
            matchingService.closeMatchesOfListing(listingId, true);
        }
        notifyFavoritersAboutStatus(listing);
        return ListingView.of(listing, favoriteRepository.countByListingId(listingId));
    }

    /** Deletes the listing with its favorites, match records and history in one transaction. */
    @Transactional
    public void delete(Long userId, Long listingId) {
        Listing listing = requireOwnedListing(userId, listingId);
        favoriteRepository.deleteByListingId(listingId);
        matchRecordRepository.deleteByListingId(listingId);
        statusHistoryRepository.deleteByListingId(listingId);
        listingRepository.delete(listing);
    }

    @Transactional(readOnly = true)
    public ListingDetailView detail(Long listingId, Long viewerId) {
        Listing listing = listingRepository.findWithSellerById(listingId)
                .orElseThrow(() -> BizException.notFound("Listing not found"));
        boolean favorite = viewerId != null && favoriteRepository.existsByUserIdAndListingId(viewerId, listingId);
        boolean owner = viewerId != null && listing.getSeller().getId().equals(viewerId);
        List<StatusHistoryView> history = statusHistoryRepository.findByListingIdOrderByChangedAtAsc(listingId)
                .stream()
                .map(item -> new StatusHistoryView(
                        item.getFromStatus() == null ? null : item.getFromStatus().name(),
                        item.getToStatus().name(),
                        item.getChangedAt()))
                .toList();
        return new ListingDetailView(ListingView.of(listing, favoriteRepository.countByListingId(listingId)),
                favorite, owner, history);
    }

    @Transactional(readOnly = true)
    public List<ListingView> myListings(Long userId) {
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(userId).stream()
                .map(listing -> ListingView.of(listing, favoriteRepository.countByListingId(listing.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Listing requireListing(Long listingId) {
        return listingRepository.findWithSellerById(listingId)
                .orElseThrow(() -> BizException.notFound("Listing not found"));
    }

    private Listing requireOwnedListing(Long userId, Long listingId) {
        Listing listing = listingRepository.findWithSellerById(listingId)
                .orElseThrow(() -> BizException.notFound("Listing not found"));
        if (!listing.getSeller().getId().equals(userId)) {
            throw BizException.forbidden("You can only manage your own listings");
        }
        return listing;
    }

    private void applyForm(Listing listing, ListingForm form) {
        listing.setTitle(form.title().trim());
        listing.setCategory(form.category());
        listing.setDescription(form.description().trim());
        listing.setCondition(form.condition());
        listing.setPrice(form.price());
        listing.setPhotoUrl(form.photoUrl() == null || form.photoUrl().isBlank() ? null : form.photoUrl().trim());
    }

    /** A failing matching run must not block publishing (REQ-5.1 error handling). */
    private void triggerMatching(Listing listing) {
        try {
            matchingService.runForListing(listing);
        } catch (RuntimeException ex) {
            log.error("Matching after listing {} failed, the scheduled run will retry", listing.getId(), ex);
        }
    }

    private void notifyFavoritersAboutPrice(Listing listing, BigDecimal oldPrice) {
        for (Long userId : favoriteRepository.findUserIdsByListingId(listing.getId())) {
            if (Objects.equals(userId, listing.getSeller().getId())) {
                continue;
            }
            notificationService.notifyUser(userId, NotificationType.PRICE_CHANGED,
                    "Price changed on a favorited item",
                    "The price of \"" + listing.getTitle() + "\" changed from MOP " + oldPrice
                            + " to MOP " + listing.getPrice() + ".",
                    listing.getId(), null);
        }
    }

    private void notifyFavoritersAboutStatus(Listing listing) {
        List<Long> favoriteUserIds = favoriteRepository.findUserIdsByListingId(listing.getId());
        if (favoriteUserIds.isEmpty()) {
            return;
        }
        for (Long userId : favoriteUserIds) {
            if (Objects.equals(userId, listing.getSeller().getId())) {
                continue;
            }
            notificationService.notifyUser(userId, NotificationType.STATUS_CHANGED,
                    "Favorited item is now " + label(listing.getStatus()),
                    "\"" + listing.getTitle() + "\" is now " + label(listing.getStatus()).toLowerCase() + ".",
                    listing.getId(), null);
        }
    }

    private String label(ListingStatus status) {
        return switch (status) {
            case AVAILABLE -> "Available";
            case RESERVED -> "Reserved";
            case SOLD -> "Sold";
        };
    }
}
