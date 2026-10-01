package com.grabseat.dto;

import com.grabseat.model.EventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventSummaryResponse(
    Long id,
    String name,
    String description,
    EventType type,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal basePrice,
    String venueName,
    String performerName,
    String screenName
) {
}
