package com.must.secondhand.service;

import com.must.secondhand.dto.MatchView;
import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.entity.MatchRecord;
import com.must.secondhand.model.enums.ItemCondition;
import com.must.secondhand.model.enums.ListingStatus;
import com.must.secondhand.model.enums.MatchStatus;
import com.must.secondhand.model.enums.NotificationType;
import com.must.secondhand.model.enums.RequestStatus;
import com.must.secondhand.repository.BuyRequestRepository;
import com.must.secondhand.repository.ListingRepository;
import com.must.secondhand.repository.MatchRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Automatic buyer-seller matching (REQ-5.1), the core feature of the platform.
 *
 * <pre>
 * candidate gate : same category AND keyword similarity >= 0.30
 *                  AND price inside the buyer budget AND condition accepted
 * score          : 0.40 * category + 0.35 * price overlap + 0.25 * condition
 * shown to users : score > 0.70
 * </pre>
 *
 * Every qualifying listing-request pair is stored as an independent match, so a listing
 * can match several buyers at the same time.
 */
@Service
public class MatchingService {

    public static final double WEIGHT_CATEGORY = 0.40;
    public static final double WEIGHT_PRICE = 0.35;
    public static final double WEIGHT_CONDITION = 0.25;
    public static final double KEYWORD_THRESHOLD = 0.30;
    public static final double SCORE_THRESHOLD = 0.70;

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final ListingRepository listingRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final NotificationService notificationService;

    public MatchingService(ListingRepository listingRepository,
                           BuyRequestRepository buyRequestRepository,
                           MatchRecordRepository matchRecordRepository,
                           NotificationService notificationService) {
        this.listingRepository = listingRepository;
        this.buyRequestRepository = buyRequestRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.notificationService = notificationService;
    }

    /** Sub-scores of one candidate pair, kept for transparency on the matches page. */
    public record PairScore(double category, double price, double condition, double keyword, double total) {
    }

    /**
     * Compares one listing with one request. Empty means "not a candidate" or below threshold.
     */
    public Optional<PairScore> score(Listing listing, BuyRequest request) {
        if (listing.getCategory() != request.getCategory()) {
            return Optional.empty();
        }
        if (listing.getStatus() != ListingStatus.AVAILABLE || request.getStatus() != RequestStatus.ACTIVE) {
            return Optional.empty();
        }
        // The buyer's acceptable condition is a hard gate.
        if (listing.getCondition().getRank() > request.getAcceptableCondition().getRank()) {
            return Optional.empty();
        }
        // The asking price must be inside the budget interval.
        BigDecimal price = listing.getPrice();
        if (price.compareTo(request.getMinBudget()) < 0 || price.compareTo(request.getMaxBudget()) > 0) {
            return Optional.empty();
        }

        String listingText = listing.getTitle() + " " + listing.getDescription();
        String requestText = request.getKeywords() + " " + nullToEmpty(request.getRequirements());
        double keywordScore = TextSimilarity.similarity(listingText, requestText);
        if (keywordScore < KEYWORD_THRESHOLD) {
            return Optional.empty();
        }

        double categoryScore = 1.0;
        double priceScore = 1.0;
        double conditionScore = conditionScore(listing.getCondition(), request.getAcceptableCondition());
        double total = round(WEIGHT_CATEGORY * categoryScore + WEIGHT_PRICE * priceScore
                + WEIGHT_CONDITION * conditionScore);
        if (total <= SCORE_THRESHOLD) {
            return Optional.empty();
        }
        return Optional.of(new PairScore(categoryScore, priceScore, conditionScore, round(keywordScore), total));
    }

    /**
     * An exact condition match scores 1.0; every grade of unused condition quality costs 25%.
     */
    private double conditionScore(ItemCondition listingCondition, ItemCondition accepted) {
        int gap = accepted.getRank() - listingCondition.getRank();
        return switch (gap) {
            case 0 -> 1.0;
            case 1 -> 0.75;
            case 2 -> 0.5;
            default -> 0.25;
        };
    }

    /** Re-runs matching for one listing against all active requests. */
    @Transactional
    public void runForListing(Listing listing) {
        // Reload so the engine always works with managed entities (callers may hold detached ones).
        Listing managed = listingRepository.findWithSellerById(listing.getId()).orElse(null);
        if (managed == null) {
            return;
        }
        if (managed.getStatus() != ListingStatus.AVAILABLE) {
            closeMatchesOfListing(managed.getId(), false);
            return;
        }
        List<BuyRequest> requests = buyRequestRepository.findByStatusAndCategory(
                RequestStatus.ACTIVE, managed.getCategory());
        for (BuyRequest request : requests) {
            upsert(managed, request, true);
        }
    }

    /** Re-runs matching for one request against all available listings. */
    @Transactional
    public void runForRequest(BuyRequest request) {
        BuyRequest managed = buyRequestRepository.findById(request.getId()).orElse(null);
        if (managed == null) {
            return;
        }
        if (managed.getStatus() != RequestStatus.ACTIVE) {
            closeMatchesOfRequest(managed.getId());
            return;
        }
        List<Listing> listings = listingRepository.findByStatusAndCategory(
                ListingStatus.AVAILABLE, managed.getCategory());
        for (Listing listing : listings) {
            upsert(listing, managed, true);
        }
    }

    /** Scheduled safety net: retries matching in case an earlier run failed (REQ-5.1 error handling). */
    @Transactional
    public int runAll() {
        // Only recent items are re-checked; older ones were already processed after publishing.
        java.time.Instant windowStart = java.time.Instant.now().minus(java.time.Duration.ofDays(1));
        List<Listing> listings = listingRepository.findRecentActive(ListingStatus.AVAILABLE, windowStart);
        List<BuyRequest> requests = buyRequestRepository.findRecentActive(RequestStatus.ACTIVE, windowStart);
        int created = 0;
        for (Listing listing : listings) {
            for (BuyRequest request : requests) {
                if (request.getCategory() != listing.getCategory()) {
                    continue;
                }
                if (upsert(listing, request, false)) {
                    created++;
                }
            }
        }
        if (created > 0) {
            log.info("Scheduled matching run created {} new match record(s)", created);
        }
        return created;
    }

    /**
     * Creates, reopens or closes the match record of a pair.
     *
     * @param notify whether a new match should notify both users
     * @return true when a new match record was created
     */
    private boolean upsert(Listing listing, BuyRequest request, boolean notify) {
        Optional<PairScore> scored = score(listing, request);
        Optional<MatchRecord> existing = matchRecordRepository
                .findByListingIdAndRequestId(listing.getId(), request.getId());

        if (scored.isEmpty()) {
            existing.filter(match -> match.getStatus() == MatchStatus.OPEN).ifPresent(match -> {
                match.setStatus(MatchStatus.CLOSED);
                match.setUpdatedAt(java.time.Instant.now());
                matchRecordRepository.save(match);
            });
            return false;
        }

        PairScore pairScore = scored.get();
        if (existing.isPresent()) {
            MatchRecord match = existing.get();
            match.setScore(pairScore.total());
            match.setCategoryScore(pairScore.category());
            match.setPriceScore(pairScore.price());
            match.setConditionScore(pairScore.condition());
            match.setKeywordScore(pairScore.keyword());
            match.setUpdatedAt(java.time.Instant.now());
            if (match.getStatus() == MatchStatus.CLOSED && listing.getStatus() == ListingStatus.AVAILABLE) {
                match.setStatus(MatchStatus.OPEN);
            }
            matchRecordRepository.save(match);
            return false;
        }

        MatchRecord match = new MatchRecord();
        match.setListing(listing);
        match.setRequest(request);
        match.setScore(pairScore.total());
        match.setCategoryScore(pairScore.category());
        match.setPriceScore(pairScore.price());
        match.setConditionScore(pairScore.condition());
        match.setKeywordScore(pairScore.keyword());
        match.setStatus(MatchStatus.OPEN);
        matchRecordRepository.save(match);

        if (notify) {
            notifyMatch(listing, request, pairScore);
        }
        return true;
    }

    private void notifyMatch(Listing listing, BuyRequest request, PairScore score) {
        String amount = "MOP " + listing.getPrice().setScale(2, RoundingMode.HALF_UP);
        notificationService.notifyUser(
                request.getBuyer().getId(),
                NotificationType.MATCH_FOUND,
                "New match for your request",
                "A listing \"" + listing.getTitle() + "\" (" + amount + ", " + listing.getCondition().getLabel()
                        + ") matches your request with score " + formatScore(score.total()) + ".",
                listing.getId(),
                request.getId());
        notificationService.notifyUser(
                listing.getSeller().getId(),
                NotificationType.MATCH_FOUND,
                "A buyer is looking for your item",
                "Buyer " + request.getBuyer().getName() + " posted a request matching \"" + listing.getTitle()
                        + "\" with score " + formatScore(score.total()) + ".",
                listing.getId(),
                request.getId());
    }

    /** Closes every open match of a listing, optionally warning the buyers (reserved / sold). */
    @Transactional
    public void closeMatchesOfListing(Long listingId, boolean notifyBuyers) {
        for (MatchRecord match : matchRecordRepository.findByListingIdAndStatus(listingId, MatchStatus.OPEN)) {
            match.setStatus(MatchStatus.CLOSED);
            match.setUpdatedAt(java.time.Instant.now());
            matchRecordRepository.save(match);
            if (notifyBuyers) {
                notificationService.notifyUser(
                        match.getRequest().getBuyer().getId(),
                        NotificationType.STATUS_CHANGED,
                        "A matched item is no longer available",
                        "The listing \"" + match.getListing().getTitle() + "\" you were matched with is now "
                                + match.getListing().getStatus().name().toLowerCase() + ".",
                        match.getListing().getId(),
                        match.getRequest().getId());
            }
        }
    }

    @Transactional
    public void closeMatchesOfRequest(Long requestId) {
        for (MatchRecord match : matchRecordRepository.findByRequestIdAndStatus(requestId, MatchStatus.OPEN)) {
            match.setStatus(MatchStatus.CLOSED);
            match.setUpdatedAt(java.time.Instant.now());
            matchRecordRepository.save(match);
        }
    }

    @Transactional(readOnly = true)
    public List<MatchView> matchesForUser(Long userId) {
        return matchRecordRepository.findOpenMatchesForUser(userId).stream()
                .map(match -> MatchView.of(match, userId))
                .toList();
    }

    private String formatScore(double score) {
        return String.format("%.2f", score);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private double round(double value) {
        return Math.round(value * 1000d) / 1000d;
    }
}
