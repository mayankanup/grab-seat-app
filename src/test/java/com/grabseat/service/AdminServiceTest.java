package com.grabseat.service;

import com.grabseat.dto.CreateEventRequest;
import com.grabseat.dto.CreateScheduledEventsRequest;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.model.*;
import com.grabseat.repository.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    VenueRepository venues;
    @Mock
    PerformerRepository performers;
    @Mock
    EventRepository events;
    @Mock
    TicketRepository tickets;
    @Mock
    EventService eventService;

    @InjectMocks
    AdminService service;

    private CreateEventRequest request() {
        return new CreateEventRequest("New Night Show", "Late comedy", EventType.COMEDY,
            3L, 4L, LocalDateTime.now().plusDays(9).withNano(0),
            LocalDateTime.now().plusDays(9).plusHours(2).withNano(0),
            new BigDecimal("499.00"), 5);
    }

    @Test
    void createEventSavesVenuePerformerEventAndTickets() {
        Venue venue = new Venue("Hall", "Town", 100);
        Performer performer = new Performer("Comic", "COMEDIAN");
        when(venues.findById(3L)).thenReturn(Optional.of(venue));
        when(performers.findById(4L)).thenReturn(Optional.of(performer));
        when(events.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));
        when(eventService.getEventDetails(any())).thenReturn(null);

        service.createEvent(request());

        verify(tickets).saveAll(org.mockito.ArgumentMatchers.argThat(
            (List<Ticket> batch) -> batch.size() == 5
                && batch.stream().allMatch(t -> t.getStatus() == TicketStatus.AVAILABLE)));
    }

    @Test
    void createEventFailsWhenPerformerMissing() {
        Venue venue = new Venue("Hall", "Town", 100);
        when(venues.findById(3L)).thenReturn(Optional.of(venue));
        when(performers.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createEvent(request()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Performer");
    }

    @Test
    void createEventFailsWhenVenueMissing() {
        when(venues.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createEvent(request()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Venue");
    }

    @Test
    void createEventFailsWhenEndBeforeStart() {
        CreateEventRequest bad = new CreateEventRequest("X", null, EventType.MOVIE, 3L, null,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1),
            new BigDecimal("100.00"), 10);

        assertThatThrownBy(() -> service.createEvent(bad))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createVenueTrimsAndSaves() {
        when(venues.save(any(Venue.class))).thenAnswer(i -> i.getArgument(0));

        var dto = service.createVenue("  Hall  ", "Town", 100);

        assertThat(dto.name()).isEqualTo("Hall");
    }

    private CreateScheduledEventsRequest scheduleRequest() {
        return new CreateScheduledEventsRequest("Morning Laughs", "Daily comedy",
            EventType.COMEDY, 3L, 4L, new BigDecimal("299.00"), 2,
            new CreateScheduledEventsRequest.Schedule(
                java.time.LocalDate.now().plusDays(10),
                java.time.LocalDate.now().plusDays(11),
                List.of(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)),
                90));
    }

    @Test
    void createScheduledEventsGeneratesTwoDaysTimesTwoShows() {
        Venue venue = new Venue("Hall", "Town", 100);
        Performer performer = new Performer("Comic", "COMEDIAN");
        when(venues.findById(3L)).thenReturn(Optional.of(venue));
        when(performers.findById(4L)).thenReturn(Optional.of(performer));
        when(events.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        var created = service.createScheduledEvents(scheduleRequest());

        assertThat(created).hasSize(4);
        assertThat(created.get(0).venueName()).isEqualTo("Hall");
        assertThat(created.get(0).performerName()).isEqualTo("Comic");
        verify(tickets, org.mockito.Mockito.times(4)).saveAll(any());
    }

    @Test
    void createScheduledEventsRejectsOverlaps() {
        var bad = new CreateScheduledEventsRequest("X", null, EventType.MOVIE, 3L, null,
            new BigDecimal("100.00"), 2,
            new CreateScheduledEventsRequest.Schedule(
                java.time.LocalDate.now().plusDays(10),
                java.time.LocalDate.now().plusDays(10),
                List.of(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(8, 30)),
                60));

        assertThatThrownBy(() -> service.createScheduledEvents(bad))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createScheduledEventsRejectsBackwardRange() {
        var bad = new CreateScheduledEventsRequest("X", null, EventType.MOVIE, 3L, null,
            new BigDecimal("100.00"), 2,
            new CreateScheduledEventsRequest.Schedule(
                java.time.LocalDate.now().plusDays(11),
                java.time.LocalDate.now().plusDays(10),
                List.of(java.time.LocalTime.of(8, 0)),
                60));

        assertThatThrownBy(() -> service.createScheduledEvents(bad))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
