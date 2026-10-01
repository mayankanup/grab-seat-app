package com.grabseat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReserveRequest(
    @NotNull Long ticketId,
    @NotBlank String userId
) {
}
