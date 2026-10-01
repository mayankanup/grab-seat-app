package com.grabseat.service;

import com.grabseat.dto.BookedTicketDto;
import com.grabseat.dto.ConfirmBookingRequest;
import com.grabseat.dto.ConfirmBookingResponse;
import com.grabseat.dto.ReserveTicketsResponse;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.exception.TicketNotAvailableException;
import com.grabseat.model.Booking;
import com.grabseat.model.BookingStatus;
import com.grabseat.model.Ticket;
import com.grabseat.payment.DummyStripeService;
import com.grabseat.repository.BookingRepository;
import com.grabseat.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingService {

    private final TicketRepository tickets;
    private final BookingRepository bookings;
    private final DummyStripeService payments;

    public BookingService(TicketRepository tickets, BookingRepository bookings,
                          DummyStripeService payments) {
        this.tickets = tickets;
        this.bookings = bookings;
        this.payments = payments;
    }

    @Transactional
    public ReserveTicketsResponse reserve(List<Long> ticketIds, Long userId) {
        List<Long> ids = requireIds(ticketIds);
        requireId(userId);
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
    public ConfirmBookingResponse confirm(Long bookingId, Long userId, ConfirmBookingRequest payment) {
        requireId(userId);
        Booking booking = bookings.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        if (!userId.equals(booking.getUserId())) {
            throw new TicketNotAvailableException(
                "Booking " + bookingId + " belongs to another user");
        }
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new TicketNotAvailableException(
                "Booking " + bookingId + " is already " + booking.getStatus());
        }
        BigDecimal total = booking.getTickets().stream()
            .map(Ticket::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        DummyStripeService.ChargeResult charge =
            payments.charge(payment.cardNumber(), total);
        for (Ticket t : booking.getTickets()) {
            t.confirm(userId);
        }
        booking.confirm();
        return new ConfirmBookingResponse(booking.getId(), booking.getStatus().name(),
            booking.getUserId(), charge.transactionId(), confirmLinesOf(booking));
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

    private void requireId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private List<BookedTicketDto> linesOf(Booking booking) {
        return booking.getTickets().stream().map(t -> new BookedTicketDto(
            t.getId(), t.getEvent() != null ? t.getEvent().getId() : null,
            t.getSeatNumber(), t.getPrice(), t.getStatus().name())).toList();
    }

    private List<BookedTicketDto> confirmLinesOf(Booking booking) {
        return linesOf(booking);
    }
}
