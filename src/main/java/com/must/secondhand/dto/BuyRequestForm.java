package com.must.secondhand.dto;

import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ItemCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Want-to-buy request form (REQ-4.1). */
public record BuyRequestForm(
        @NotBlank(message = "Desired item keywords are required")
        @Size(max = 200, message = "Keywords must be at most 200 characters")
        String keywords,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Minimum budget is required")
        @DecimalMin(value = "0.01", message = "Minimum budget must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Budget must have at most 2 decimal places")
        BigDecimal minBudget,

        @NotNull(message = "Maximum budget is required")
        @DecimalMin(value = "0.01", message = "Maximum budget must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Budget must have at most 2 decimal places")
        BigDecimal maxBudget,

        @NotNull(message = "Acceptable condition is required")
        ItemCondition acceptableCondition,

        @Size(max = 500, message = "Requirements must be at most 500 characters")
        String requirements
) {
}
