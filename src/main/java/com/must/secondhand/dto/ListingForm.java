package com.must.secondhand.dto;

import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ItemCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Publish / edit listing form (REQ-2.1): at most five mandatory fields. */
public record ListingForm(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @NotNull(message = "Category is required")
        Category category,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Condition is required")
        ItemCondition condition,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
        BigDecimal price,

        String photoUrl
) {
}
