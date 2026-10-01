package com.grabseat.dto;

import java.util.List;

public record ConfirmBookingResponse(
    Long bookingId,
    String bookingStatus,
    Long userId,
    String paymentReference,
    List<BookedTicketDto> tickets
) {
}
