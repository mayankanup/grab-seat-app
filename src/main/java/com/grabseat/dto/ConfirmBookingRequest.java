package com.grabseat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmBookingRequest(
    @NotNull Long bookingId,
    @NotBlank String paymentToken
) {
}
