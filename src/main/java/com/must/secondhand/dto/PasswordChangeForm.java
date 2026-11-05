package com.must.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Password change, requires the old password (REQ-1.2). */
public record PasswordChangeForm(
        @NotBlank(message = "Current password is required") String oldPassword,
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
        String newPassword
) {
}
