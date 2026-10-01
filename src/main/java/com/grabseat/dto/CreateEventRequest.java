package com.grabseat.dto;

import com.grabseat.model.EventType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateEventRequest(
    @NotBlank String name,
    String description,
    @NotNull EventType type,
    @NotNull Long venueId,
    Long performerId,
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal basePrice,
    @NotNull @Min(1) @Max(1000) Integer ticketCount
) {
}
