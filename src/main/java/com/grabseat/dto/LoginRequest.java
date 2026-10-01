package com.grabseat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank String userId,
    @NotBlank @Size(min = 8, max = 72) String password
) {
}
