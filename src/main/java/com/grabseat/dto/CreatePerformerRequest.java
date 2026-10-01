package com.grabseat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePerformerRequest(
    @NotBlank String name,
    @NotBlank String type
) {
}
