package com.grabseat.service;

import com.grabseat.dto.*;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.model.Event;
import com.grabseat.repository.EventRepository;
import com.grabseat.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository events;
    private final TicketRepository tickets;

    public EventService(EventRepository events, TicketRepository tickets) {
        this.events = events;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public EventDetailsResponse getEventDetails(Long eventId) {
        Event event = events.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        VenueDto venue = new VenueDto(
            event.getVenue().getId(),
            event.getVenue().getName(),
            event.getVenue().getLocation(),
            event.getVenue().getCapacity());

        PerformerDto performer = event.getPerformer() == null ? null : new PerformerDto(
            event.getPerformer().getId(),
            event.getPerformer().getName(),
            event.getPerformer().getType());

        List<TicketDto> ticketDtos = tickets.findByEventId(eventId).stream()
            .map(t -> new TicketDto(t.getId(), t.getSeatNumber(), t.getPrice(), t.getStatus()))
            .toList();

        return new EventDetailsResponse(
            event.getId(), event.getName(), event.getDescription(), event.getType(),
            event.getStartTime(), event.getEndTime(), event.getBasePrice(),
            venue, performer, ticketDtos);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> search(String keyword, LocalDateTime start,
                                            LocalDateTime end, int page, int pageSize) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("startTime").ascending());
        return events.search(keyword, start, end, pageable).map(e -> new EventSummaryResponse(
            e.getId(), e.getName(), e.getDescription(), e.getType(),
            e.getStartTime(), e.getEndTime(), e.getBasePrice(),
            e.getVenue() != null ? e.getVenue().getName() : null,
            e.getPerformer() != null ? e.getPerformer().getName() : null));
    }
}
