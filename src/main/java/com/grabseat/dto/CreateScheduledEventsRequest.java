package com.grabseat.dto;

import com.grabseat.model.EventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateScheduledEventsRequest(
    @NotBlank String name,
    String description,
    @NotNull EventType type,
    @NotNull Long venueId,
    Long performerId,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal basePrice,
    @NotNull @Min(1) @Max(1000) Integer ticketCount,
    @NotNull @Valid Schedule schedule
) {
    public record Schedule(
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @NotNull @Size(min = 1, max = 8) List<@NotNull LocalTime> showTimes,
        @NotNull @Min(15) @Max(600) Integer durationMinutes
    ) {
    }
}
