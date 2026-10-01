package com.grabseat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmRequest(
    @NotNull Long ticketId,
    @NotBlank String userId,
    @NotBlank String paymentToken
) {
}
