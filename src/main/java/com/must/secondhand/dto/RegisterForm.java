package com.must.secondhand.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registration form (REQ-1.1): five input fields. */
public record RegisterForm(
        @NotBlank(message = "Name is required")
        @Size(max = 60, message = "Name must be at most 60 characters")
        String name,

        @NotBlank(message = "Student number is required")
        @Pattern(regexp = "^[0-9]{6,20}$", message = "Student number must be 6-20 digits")
        String studentNo,

        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail format is invalid")
        @Size(max = 120, message = "E-mail must be at most 120 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Please repeat the password")
        String confirmPassword
) {
}
