package com.must.secondhand.dto;

import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** Read model of a want-to-buy request (REQ-4.1). */
public record RequestView(
        Long id,
        String keywords,
        String category,
        String categoryLabel,
        BigDecimal minBudget,
        BigDecimal maxBudget,
        String acceptableCondition,
        String acceptableConditionLabel,
        String requirements,
        RequestStatus status,
        Long buyerId,
        String buyerName,
        Instant createdAt
) {

    public static RequestView of(BuyRequest request) {
        return new RequestView(
                request.getId(),
                request.getKeywords(),
                request.getCategory().name(),
                request.getCategory().getLabel(),
                request.getMinBudget(),
                request.getMaxBudget(),
                request.getAcceptableCondition().name(),
                request.getAcceptableCondition().getLabel(),
                request.getRequirements(),
                request.getStatus(),
                request.getBuyer().getId(),
                request.getBuyer().getName(),
                request.getCreatedAt());
    }
}
