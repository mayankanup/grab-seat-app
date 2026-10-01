package com.grabseat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,32}",
        message = "login must be 3-32 chars: letters, digits, . _ -") String login,
    @NotBlank @Size(min = 8, max = 72,
        message = "password must be 8-72 chars") String password,
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank @Email @Size(max = 254) String email
) {
}
