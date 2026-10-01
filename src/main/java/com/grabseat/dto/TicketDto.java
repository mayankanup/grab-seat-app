package com.grabseat.dto;

import com.grabseat.model.TicketStatus;
import java.math.BigDecimal;

public record TicketDto(Long id, String seatNumber, BigDecimal price, TicketStatus status) {
}
