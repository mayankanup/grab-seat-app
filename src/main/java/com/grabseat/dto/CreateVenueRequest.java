package com.grabseat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVenueRequest(
    @NotBlank String name,
    @NotBlank String location,
    @NotNull @Min(1) @Max(100000) Integer capacity
) {
}
