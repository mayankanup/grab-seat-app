package com.grabseat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ConfirmBookingRequest(
    @NotNull Long bookingId,
    @NotBlank @Pattern(regexp = "[\\d ]{12,23}", message = "cardNumber must be digits") String cardNumber,
    @NotNull @Min(1) @Max(12) Integer expMonth,
    @NotNull @Min(2026) Integer expYear,
    @NotBlank @Pattern(regexp = "\\d{3,4}", message = "cvc must be 3-4 digits") String cvc
) {
}
