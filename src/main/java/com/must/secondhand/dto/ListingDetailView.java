package com.must.secondhand.dto;

import java.util.List;

/** Listing detail payload: the listing plus viewer-specific flags and its status history. */
public record ListingDetailView(
        ListingView listing,
        boolean favorite,
        boolean owner,
        List<StatusHistoryView> history
) {
}
