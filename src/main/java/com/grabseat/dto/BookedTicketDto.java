package com.grabseat.dto;

import java.math.BigDecimal;

public record BookedTicketDto(
    Long ticketId,
    Long eventId,
    String seatNumber,
    BigDecimal price,
    String ticketStatus
) {
}
