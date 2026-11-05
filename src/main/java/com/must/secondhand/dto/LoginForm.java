package com.must.secondhand.dto;

import jakarta.validation.constraints.NotBlank;

/** Login form; the identifier may be an e-mail address or a student number (REQ-1.2). */
public record LoginForm(
        @NotBlank(message = "E-mail or student number is required") String identifier,
        @NotBlank(message = "Password is required") String password
) {
}
