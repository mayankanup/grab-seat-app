package com.grabseat.service;

import com.grabseat.dto.EventDetailsResponse;
import com.grabseat.model.*;
import com.grabseat.repository.EventRepository;
import com.grabseat.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    EventRepository events;
    @Mock
    TicketRepository tickets;

    @InjectMocks
    EventService service;

    @Test
    void returnsEventWithVenuePerformerAndTickets() {
        Venue venue = new Venue("PVR Downtown Cinema", "Downtown Mall", 120);
        Performer performer = new Performer("Zakir Khan", "COMEDIAN");
        Event event = new Event("Zakir Live", "Stand-up", EventType.COMEDY, venue, performer,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
            new BigDecimal("999.00"));

        when(events.findById(7L)).thenReturn(Optional.of(event));
        when(tickets.findByEventId(7L)).thenReturn(List.of(
            new Ticket(event, "A-1", new BigDecimal("999.00"), TicketStatus.AVAILABLE)));

        EventDetailsResponse res = service.getEventDetails(7L);

        assertThat(res.name()).isEqualTo("Zakir Live");
        assertThat(res.venue().name()).isEqualTo("PVR Downtown Cinema");
        assertThat(res.performer().name()).isEqualTo("Zakir Khan");
        assertThat(res.tickets()).hasSize(1);
    }

    @Test
    void searchReturnsSummaries() {
        Venue venue = new Venue("PVR Downtown Cinema", "Downtown Mall", 120);
        Performer performer = new Performer("Dune Cast", "MOVIE_CAST");
        Event event = new Event("Dune Evening Show", "IMAX", EventType.MOVIE, venue, performer,
            LocalDateTime.parse("2026-10-02T18:00:00"), LocalDateTime.parse("2026-10-02T21:00:00"),
            new BigDecimal("349.00"));

        when(events.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(event)));

        var page = service.search("dune", null, null, 0, 20);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).name()).isEqualTo("Dune Evening Show");
        assertThat(page.getContent().get(0).venueName()).isEqualTo("PVR Downtown Cinema");
    }
}
