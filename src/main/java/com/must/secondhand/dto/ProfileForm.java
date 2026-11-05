package com.must.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Editable profile fields (REQ-1.2). */
public record ProfileForm(
        @NotBlank(message = "Name is required")
        @Size(max = 60, message = "Name must be at most 60 characters")
        String name,

        @Size(max = 120, message = "Contact must be at most 120 characters")
        String contact
) {
}
