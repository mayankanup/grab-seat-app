package com.grabseat.service;

import com.grabseat.dto.BookingResponse;
import com.grabseat.exception.PaymentFailedException;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.model.Booking;
import com.grabseat.model.BookingStatus;
import com.grabseat.model.Ticket;
import com.grabseat.repository.BookingRepository;
import com.grabseat.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final TicketRepository tickets;
    private final BookingRepository bookings;

    public BookingService(TicketRepository tickets, BookingRepository bookings) {
        this.tickets = tickets;
        this.bookings = bookings;
    }

    @Transactional
    public BookingResponse reserve(Long ticketId, String userId) {
        requireText(userId, "userId is required");
        Ticket ticket = tickets.findByIdForUpdate(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        ticket.reserve(userId);
        Booking booking = bookings.save(new Booking(userId, ticket, BookingStatus.RESERVED));
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse confirm(Long ticketId, String userId, String paymentToken) {
        requireText(userId, "userId is required");
        requireText(paymentToken, "paymentToken is required (Stripe mock)");
        Booking booking = bookings.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("No reservation for ticket: " + ticketId));
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new com.grabseat.exception.TicketNotAvailableException(
                "Booking " + booking.getId() + " is already " + booking.getStatus());
        }
        if (!booking.getUserId().equals(userId)) {
            throw new com.grabseat.exception.TicketNotAvailableException(
                "Ticket " + ticketId + " is reserved by another user");
        }
        chargeMock(paymentToken, booking);
        booking.getTicket().confirm(userId);
        booking.confirm();
        return toResponse(booking);
    }

    private void chargeMock(String paymentToken, Booking booking) {
        if (paymentToken.startsWith("fail")) {
            throw new PaymentFailedException("Card declined for booking " + booking.getId());
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private BookingResponse toResponse(Booking booking) {
        Ticket t = booking.getTicket();
        return new BookingResponse(
            booking.getId(), t.getId(),
            t.getEvent() != null ? t.getEvent().getId() : null,
            t.getSeatNumber(), t.getPrice(),
            t.getStatus().name(), booking.getStatus().name(), booking.getUserId());
    }
}
