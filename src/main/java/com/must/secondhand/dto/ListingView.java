package com.must.secondhand.dto;

import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.enums.ItemCondition;
import com.must.secondhand.model.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read model of a listing. Only the seller's display name is exposed,
 * personal data stays private (SEC-1.2).
 */
public record ListingView(
        Long id,
        String title,
        String category,
        String categoryLabel,
        String description,
        String condition,
        String conditionLabel,
        BigDecimal price,
        ListingStatus status,
        String photoUrl,
        Long sellerId,
        String sellerName,
        Instant createdAt,
        Instant updatedAt,
        Instant soldAt,
        long favoriteCount
) {

    public static ListingView of(Listing listing) {
        return of(listing, 0L);
    }

    public static ListingView of(Listing listing, long favoriteCount) {
        ItemCondition condition = listing.getCondition();
        return new ListingView(
                listing.getId(),
                listing.getTitle(),
                listing.getCategory().name(),
                listing.getCategory().getLabel(),
                listing.getDescription(),
                condition == null ? null : condition.name(),
                condition == null ? null : condition.getLabel(),
                listing.getPrice(),
                listing.getStatus(),
                listing.getPhotoUrl(),
                listing.getSeller().getId(),
                listing.getSeller().getName(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),
                listing.getSoldAt(),
                favoriteCount);
    }
}
