package com.grabseat.dto;

import com.grabseat.model.EventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventDetailsResponse(
    Long id,
    String name,
    String description,
    EventType type,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal basePrice,
    VenueDto venue,
    PerformerDto performer,
    List<TicketDto> tickets
) {
}
