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

    private static final Long ANUP = 7L;
    private static final Long OTHER = 9L;
    private static final String GOOD_CARD = "4242424242424242";
    private static final String BAD_CARD = "4000000000000002";

    private ConfirmBookingRequest card(String number) {
        return new ConfirmBookingRequest(1L, number, 12, 2030, "123");
    }

    private List<Ticket> availableTickets(String... seats) {
        Venue venue = new Venue("PVR Downtown Cinema", "Downtown Mall", 120);
        Performer performer = new Performer("Dune Cast", "MOVIE_CAST");
        Event event = new Event("Dune Evening Show", "IMAX", EventType.MOVIE, venue, performer,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3),
            new BigDecimal("349.00"));
        List<Ticket> list = new ArrayList<>();
        for (String s : seats) {
            list.add(new Ticket(event, s, new BigDecimal("349.00"), TicketStatus.AVAILABLE));
        }
        return list;
    }

    @Test
    void reserveMultipleTicketsCreatesOneBooking() {
        List<Ticket> locked = availableTickets("A-1", "A-2");
        when(tickets.findAllByIdInForUpdate(List.of(10L, 11L))).thenReturn(locked);
        when(bookings.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        ReserveTicketsResponse res = service.reserve(List.of(11L, 10L), ANUP);

        assertThat(res.tickets()).hasSize(2);
        assertThat(res.tickets()).allMatch(t -> t.ticketStatus().equals("RESERVED"));
        assertThat(res.bookingStatus()).isEqualTo("RESERVED");
        assertThat(res.userId()).isEqualTo(ANUP);
    }

    @Test
    void reserveConflictWhenAnyTicketTaken() {
        List<Ticket> locked = availableTickets("A-1", "A-2");
        locked.get(0).reserve(ANUP);
        when(tickets.findAllByIdInForUpdate(List.of(10L, 11L))).thenReturn(locked);

        assertThatThrownBy(() -> service.reserve(List.of(10L, 11L), OTHER))
            .isInstanceOf(TicketNotAvailableException.class);
    }

    @Test
    void reserveFailsWhenTicketMissing() {
        when(tickets.findAllByIdInForUpdate(List.of(10L, 99L)))
            .thenReturn(availableTickets("A-1"));

        assertThatThrownBy(() -> service.reserve(List.of(10L, 99L), ANUP))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmMarksAllTicketsBooked() {
        List<Ticket> locked = availableTickets("A-1", "A-2");
        locked.forEach(t -> t.reserve(ANUP));
        Booking booking = new Booking(ANUP, locked);
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        when(payments.charge(eq(GOOD_CARD), any())).thenReturn(
            new DummyStripeService.ChargeResult("ch_test", "4242", new BigDecimal("698.00")));

        ConfirmBookingResponse res = service.confirm(1L, ANUP, card(GOOD_CARD));

        assertThat(res.tickets()).allMatch(t -> t.ticketStatus().equals("BOOKED"));
        assertThat(res.bookingStatus()).isEqualTo("CONFIRMED");
        assertThat(res.paymentReference()).isEqualTo("ch_test");
    }

    @Test
    void confirmFailsForOtherUser() {
        List<Ticket> locked = availableTickets("A-1");
        locked.forEach(t -> t.reserve(ANUP));
        when(bookings.findById(1L))
            .thenReturn(Optional.of(new Booking(ANUP, locked)));

        assertThatThrownBy(() -> service.confirm(1L, OTHER, card(GOOD_CARD)))
            .isInstanceOf(TicketNotAvailableException.class);
    }

    @Test
    void confirmFailsWhenCardDeclined() {
        List<Ticket> locked = availableTickets("A-1");
        locked.forEach(t -> t.reserve(ANUP));
        when(bookings.findById(1L))
            .thenReturn(Optional.of(new Booking(ANUP, locked)));
        when(payments.charge(eq(BAD_CARD), any()))
            .thenThrow(new PaymentFailedException("Card declined (dummy Stripe)"));

        assertThatThrownBy(() -> service.confirm(1L, ANUP, card(BAD_CARD)))
            .isInstanceOf(PaymentFailedException.class);
    }
}
