package com.grabseat.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReserveTicketsResponse(
    Long bookingId,
    String bookingStatus,
    String userId,
    List<BookedTicketDto> tickets
) {
    public record BookedTicketDto(
        Long ticketId,
        Long eventId,
        String seatNumber,
        BigDecimal price,
        String ticketStatus
    ) {
    }
}
