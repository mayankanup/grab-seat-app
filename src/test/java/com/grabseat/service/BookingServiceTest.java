package com.grabseat.service;

import com.grabseat.dto.ConfirmBookingRequest;
import com.grabseat.dto.ConfirmBookingResponse;
import com.grabseat.dto.ReserveTicketsResponse;
import com.grabseat.exception.PaymentFailedException;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.exception.TicketNotAvailableException;
import com.grabseat.model.*;
import com.grabseat.payment.DummyStripeService;
import com.grabseat.repository.BookingRepository;
import com.grabseat.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    TicketRepository tickets;
    @Mock
    BookingRepository bookings;
    @Mock
    DummyStripeService payments;

    @InjectMocks
    BookingService service;

    private static final String GOOD_CARD = "4242424242424242";
    private static final String BAD_CARD = "4000000000000002";

    private ConfirmBookingRequest card(String number) {
        return new ConfirmBookingRequest(1L, number, 12, 2030, "123");
    }

    private Event event() {
        Venue venue = new Venue("PVR Downtown Cinema", "Downtown Mall", 120);
        Performer performer = new Performer("Dune Cast", "MOVIE_CAST");
        return new Event("Dune Evening Show", "IMAX", EventType.MOVIE, venue, performer,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3),
            new BigDecimal("349.00"));
    }

    private List<Ticket> availableTickets(String... seats) {
        Event e = event();
        List<Ticket> list = new ArrayList<>();
        for (String s : seats) {
            list.add(new Ticket(e, s, new BigDecimal("349.00"), TicketStatus.AVAILABLE));
        }
        return list;
    }

    @Test
    void reserveMultipleTicketsCreatesOneBooking() {
        List<Ticket> locked = availableTickets("A-1", "A-2");
        when(tickets.findAllByIdInForUpdate(List.of(10L, 11L))).thenReturn(locked);
        when(bookings.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        ReserveTicketsResponse res = service.reserve(List.of(11L, 10L), "user-1");

        assertThat(res.tickets()).hasSize(2);
        assertThat(res.tickets()).allMatch(t -> t.ticketStatus().equals("RESERVED"));
        assertThat(res.bookingStatus()).isEqualTo("RESERVED");
        assertThat(res.userId()).isEqualTo("user-1");
    }

    @Test
    void reserveConflictWhenAnyTicketTaken() {
        List<Ticket> locked = availableTickets("A-1", "A-2");
        locked.get(0).reserve("user-1");
        when(tickets.findAllByIdInForUpdate(List.of(10L, 11L))).thenReturn(locked);

        assertThatThrownBy(() -> service.reserve(List.of(10L, 11L), "user-2"))
            .isInstanceOf(TicketNotAvailableException.class);
    }

    @Test
    void reserveFailsWhenTicketMissing() {
        when(tickets.findAllByIdInForUpdate(List.of(10L, 99L)))
            .thenReturn(availableTickets("A-1"));

        assertThatThrownBy(() -> service.reserve(List.of(10L, 99L), "user-1"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmMarksAllTicketsBooked() {
        List<Ticket> locked = availableTickets("A-1", "A-2");
        locked.forEach(t -> t.reserve("user-1"));
        Booking booking = new Booking("user-1", locked);
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        when(payments.charge(eq(GOOD_CARD), any())).thenReturn(
            new DummyStripeService.ChargeResult("ch_test", "4242", new BigDecimal("698.00")));

        ConfirmBookingResponse res = service.confirm(1L, "user-1", card(GOOD_CARD));

        assertThat(res.tickets()).allMatch(t -> t.ticketStatus().equals("BOOKED"));
        assertThat(res.bookingStatus()).isEqualTo("CONFIRMED");
        assertThat(res.paymentReference()).isEqualTo("ch_test");
    }

    @Test
    void confirmFailsForOtherUser() {
        List<Ticket> locked = availableTickets("A-1");
        locked.forEach(t -> t.reserve("user-1"));
        when(bookings.findById(1L))
            .thenReturn(Optional.of(new Booking("user-1", locked)));

        assertThatThrownBy(() -> service.confirm(1L, "user-2", card(GOOD_CARD)))
            .isInstanceOf(TicketNotAvailableException.class);
    }

    @Test
    void confirmFailsWhenCardDeclined() {
        List<Ticket> locked = availableTickets("A-1");
        locked.forEach(t -> t.reserve("user-1"));
        when(bookings.findById(1L))
            .thenReturn(Optional.of(new Booking("user-1", locked)));
        when(payments.charge(eq(BAD_CARD), any()))
            .thenThrow(new PaymentFailedException("Card declined (dummy Stripe)"));

        assertThatThrownBy(() -> service.confirm(1L, "user-1", card(BAD_CARD)))
            .isInstanceOf(PaymentFailedException.class);
    }
}
