package com.must.secondhand.dto;

import com.must.secondhand.model.entity.MatchRecord;

import java.time.Instant;

/**
 * One automatic match shown on the Matches page (REQ-5.1).
 * Sub-scores are returned so the score stays transparent for the user.
 */
public record MatchView(
        Long id,
        double score,
        double categoryScore,
        double priceScore,
        double conditionScore,
        double keywordScore,
        String myRole,
        Instant createdAt,
        ListingView listing,
        RequestView request
) {

    public static MatchView of(MatchRecord match, Long viewerId) {
        String role = match.getListing().getSeller().getId().equals(viewerId) ? "SELLER" : "BUYER";
        return new MatchView(
                match.getId(),
                round(match.getScore()),
                round(match.getCategoryScore()),
                round(match.getPriceScore()),
                round(match.getConditionScore()),
                round(match.getKeywordScore()),
                role,
                match.getCreatedAt(),
                ListingView.of(match.getListing()),
                RequestView.of(match.getRequest()));
    }

    private static double round(double value) {
        return Math.round(value * 1000d) / 1000d;
    }
}
