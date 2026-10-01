package com.grabseat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReserveTicketsRequest(
    @NotEmpty List<@NotNull Long> ticketIds
) {
}
