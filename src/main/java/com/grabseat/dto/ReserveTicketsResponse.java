package com.grabseat.dto;

import java.util.List;

public record ReserveTicketsResponse(
    Long bookingId,
    String bookingStatus,
    Long userId,
    List<BookedTicketDto> tickets
) {
}
