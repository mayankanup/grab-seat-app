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
import org.springframework.data.jpa.domain.Specification;
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

        ScreenDto screen = event.getScreen() == null ? null : new ScreenDto(
            event.getScreen().getId(), event.getVenue().getId(), event.getVenue().getName(),
            event.getScreen().getName(),
            event.getScreen().getCapacity());

        List<TicketDto> ticketDtos = tickets.findByEventId(eventId).stream()
            .map(t -> new TicketDto(t.getId(), t.getSeatNumber(), t.getPrice(), t.getStatus()))
            .toList();

        return new EventDetailsResponse(
            event.getId(), event.getName(), event.getDescription(), event.getType(),
            event.getStartTime(), event.getEndTime(), event.getBasePrice(),
            venue, performer, screen, ticketDtos);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> search(String keyword, LocalDateTime start,
                                            LocalDateTime end, int page, int pageSize) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("startTime").ascending());
        Specification<Event> spec = Specification.where(null);
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), kw),
                cb.like(cb.lower(root.get("description")), kw)));
        }
        if (start != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), start));
        }
        if (end != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), end));
        }
        return events.findAll(spec, pageable).map(e -> new EventSummaryResponse(
            e.getId(), e.getName(), e.getDescription(), e.getType(),
            e.getStartTime(), e.getEndTime(), e.getBasePrice(),
            e.getVenue() != null ? e.getVenue().getName() : null,
            e.getPerformer() != null ? e.getPerformer().getName() : null,
            e.getScreen() != null ? e.getScreen().getName() : null));
    }
}
