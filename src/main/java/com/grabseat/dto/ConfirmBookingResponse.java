package com.grabseat.dto;

import java.math.BigDecimal;
import java.util.List;

public record ConfirmBookingResponse(
    Long bookingId,
    String bookingStatus,
    String userId,
    String paymentReference,
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
