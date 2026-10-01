package com.grabseat.service;

import com.grabseat.dto.BookingResponse;
import com.grabseat.exception.PaymentFailedException;
import com.grabseat.exception.TicketNotAvailableException;
import com.grabseat.model.*;
import com.grabseat.repository.BookingRepository;
import com.grabseat.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    TicketRepository tickets;
    @Mock
    BookingRepository bookings;

    @InjectMocks
    BookingService service;

    private Ticket availableTicket() {
        Venue venue = new Venue("PVR Downtown Cinema", "Downtown Mall", 120);
        Performer performer = new Performer("Dune Cast", "MOVIE_CAST");
        Event event = new Event("Dune Evening Show", "IMAX", EventType.MOVIE, venue, performer,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3),
            new BigDecimal("349.00"));
        return new Ticket(event, "A-1", new BigDecimal("349.00"), TicketStatus.AVAILABLE);
    }

    @Test
    void reserveMarksTicketReservedAndCreatesBooking() {
        Ticket ticket = availableTicket();
        when(tickets.findByIdForUpdate(10L)).thenReturn(Optional.of(ticket));
        when(bookings.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse res = service.reserve(10L, "user-1");

        assertThat(res.ticketStatus()).isEqualTo("RESERVED");
        assertThat(res.bookingStatus()).isEqualTo("RESERVED");
        assertThat(res.userId()).isEqualTo("user-1");
    }

    @Test
    void reserveConflictWhenAlreadyReserved() {
        Ticket ticket = availableTicket();
        ticket.reserve("user-1");
        when(tickets.findByIdForUpdate(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.reserve(10L, "user-2"))
            .isInstanceOf(TicketNotAvailableException.class);
    }

    @Test
    void confirmMarksTicketBooked() {
        Ticket ticket = availableTicket();
        ticket.reserve("user-1");
        Booking booking = new Booking("user-1", ticket, BookingStatus.RESERVED);
        when(bookings.findByTicketId(10L)).thenReturn(Optional.of(booking));

        BookingResponse res = service.confirm(10L, "user-1", "tok_test_visa");

        assertThat(res.ticketStatus()).isEqualTo("BOOKED");
        assertThat(res.bookingStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirmFailsForOtherUser() {
        Ticket ticket = availableTicket();
        ticket.reserve("user-1");
        Booking booking = new Booking("user-1", ticket, BookingStatus.RESERVED);
        when(bookings.findByTicketId(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.confirm(10L, "user-2", "tok_test"))
            .isInstanceOf(TicketNotAvailableException.class);
    }

    @Test
    void confirmFailsWhenCardDeclined() {
        Ticket ticket = availableTicket();
        ticket.reserve("user-1");
        Booking booking = new Booking("user-1", ticket, BookingStatus.RESERVED);
        when(bookings.findByTicketId(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.confirm(10L, "user-1", "fail_card"))
            .isInstanceOf(PaymentFailedException.class);
    }
}
