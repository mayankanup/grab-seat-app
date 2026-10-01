package com.grabseat.dto;

import java.math.BigDecimal;

public record BookingResponse(
    Long bookingId,
    Long ticketId,
    Long eventId,
    String seatNumber,
    BigDecimal price,
    String ticketStatus,
    String bookingStatus,
    String userId
) {
}
