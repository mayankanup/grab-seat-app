package com.grabseat.service;

import com.grabseat.dto.ConfirmBookingResponse;
import com.grabseat.dto.ReserveTicketsResponse;
import com.grabseat.exception.PaymentFailedException;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.exception.TicketNotAvailableException;
import com.grabseat.model.Booking;
import com.grabseat.model.BookingStatus;
import com.grabseat.model.Ticket;
import com.grabseat.repository.BookingRepository;
import com.grabseat.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingService {

    private final TicketRepository tickets;
    private final BookingRepository bookings;

    public BookingService(TicketRepository tickets, BookingRepository bookings) {
        this.tickets = tickets;
        this.bookings = bookings;
    }

    @Transactional
    public ReserveTicketsResponse reserve(List<Long> ticketIds, String userId) {
        List<Long> ids = requireIds(ticketIds);
        requireText(userId, "userId is required");
        List<Ticket> locked = tickets.findAllByIdInForUpdate(ids);
        if (locked.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more tickets not found: " + ids);
        }
        for (Ticket t : locked) {
            t.reserve(userId);
        }
        Booking booking = bookings.save(new Booking(userId, locked));
        return new ReserveTicketsResponse(booking.getId(), booking.getStatus().name(),
            booking.getUserId(), linesOf(booking));
    }

    @Transactional
    public ConfirmBookingResponse confirm(Long bookingId, String userId, String paymentToken) {
        requireText(userId, "userId is required");
        requireText(paymentToken, "paymentToken is required (Stripe mock)");
        Booking booking = bookings.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        if (!booking.getUserId().equals(userId)) {
            throw new TicketNotAvailableException(
                "Booking " + bookingId + " belongs to another user");
        }
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new TicketNotAvailableException(
                "Booking " + bookingId + " is already " + booking.getStatus());
        }
        chargeMock(paymentToken, booking);
        for (Ticket t : booking.getTickets()) {
            t.confirm(userId);
        }
        booking.confirm();
        return new ConfirmBookingResponse(booking.getId(), booking.getStatus().name(),
            booking.getUserId(), confirmLinesOf(booking));
    }

    private void chargeMock(String paymentToken, Booking booking) {
        if (paymentToken.startsWith("fail")) {
            throw new PaymentFailedException("Card declined for booking " + booking.getId());
        }
    }

    private List<Long> requireIds(List<Long> ticketIds) {
        if (ticketIds == null || ticketIds.isEmpty() || ticketIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("ticketIds must contain at least one ticket id");
        }
        return new ArrayList<>(new HashSet<>(ticketIds)).stream().sorted().toList();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private List<ReserveTicketsResponse.BookedTicketDto> linesOf(Booking booking) {
        return booking.getTickets().stream().map(t -> new ReserveTicketsResponse.BookedTicketDto(
            t.getId(), t.getEvent() != null ? t.getEvent().getId() : null,
            t.getSeatNumber(), t.getPrice(), t.getStatus().name())).toList();
    }

    private List<ConfirmBookingResponse.BookedTicketDto> confirmLinesOf(Booking booking) {
        return booking.getTickets().stream().map(t -> new ConfirmBookingResponse.BookedTicketDto(
            t.getId(), t.getEvent() != null ? t.getEvent().getId() : null,
            t.getSeatNumber(), t.getPrice(), t.getStatus().name())).toList();
    }
}
