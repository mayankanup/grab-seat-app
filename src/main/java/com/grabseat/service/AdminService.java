package com.grabseat.service;

import com.grabseat.dto.*;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.model.*;
import com.grabseat.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final VenueRepository venues;
    private final PerformerRepository performers;
    private final EventRepository events;
    private final TicketRepository tickets;
    private final EventService eventService;

    public AdminService(VenueRepository venues, PerformerRepository performers,
                        EventRepository events, TicketRepository tickets,
                        EventService eventService) {
        this.venues = venues;
        this.performers = performers;
        this.events = events;
        this.tickets = tickets;
        this.eventService = eventService;
    }

    @Transactional
    public VenueDto createVenue(String name, String location, Integer capacity) {
        Venue saved = venues.save(new Venue(name.trim(), location.trim(), capacity));
        return new VenueDto(saved.getId(), saved.getName(), saved.getLocation(),
            saved.getCapacity());
    }

    @Transactional
    public PerformerDto createPerformer(String name, String type) {
        Performer saved = performers.save(new Performer(name.trim(), type.trim()));
        return new PerformerDto(saved.getId(), saved.getName(), saved.getType());
    }

    @Transactional(readOnly = true)
    public List<VenueDto> listVenues() {
        return venues.findAll(Sort.by("id")).stream()
            .map(v -> new VenueDto(v.getId(), v.getName(), v.getLocation(), v.getCapacity()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PerformerDto> listPerformers() {
        return performers.findAll(Sort.by("id")).stream()
            .map(p -> new PerformerDto(p.getId(), p.getName(), p.getType()))
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> listEvents(int page, int pageSize) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        return events.findAll(PageRequest.of(safePage, safeSize, Sort.by("startTime").ascending()))
            .map(e -> new EventSummaryResponse(e.getId(), e.getName(), e.getDescription(),
                e.getType(), e.getStartTime(), e.getEndTime(), e.getBasePrice(),
                e.getVenue() != null ? e.getVenue().getName() : null,
                e.getPerformer() != null ? e.getPerformer().getName() : null));
    }

    @Transactional
    public VenueDto updateVenue(Long id, String name, String location, Integer capacity) {
        Venue venue = venues.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + id));
        venue.update(name.trim(), location.trim(), capacity);
        return new VenueDto(venue.getId(), venue.getName(), venue.getLocation(),
            venue.getCapacity());
    }

    @Transactional
    public PerformerDto updatePerformer(Long id, String name, String type) {
        Performer performer = performers.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Performer not found: " + id));
        performer.update(name.trim(), type.trim());
        return new PerformerDto(performer.getId(), performer.getName(), performer.getType());
    }

    @Transactional
    public EventDetailsResponse updateEvent(Long id, UpdateEventRequest req) {
        if (req.endTime() != null && !req.endTime().isAfter(req.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        Event event = events.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        Venue venue = venues.findById(req.venueId())
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + req.venueId()));
        Performer performer = null;
        if (req.performerId() != null) {
            performer = performers.findById(req.performerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Performer not found: " + req.performerId()));
        }
        event.update(req.name().trim(), req.description(), req.type(), venue, performer,
            req.startTime(), req.endTime(), req.basePrice());
        return eventService.getEventDetails(event.getId());
    }

    @Transactional
    public EventDetailsResponse createEvent(CreateEventRequest req) {
        if (req.endTime() != null && !req.endTime().isAfter(req.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        Venue venue = venues.findById(req.venueId())
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + req.venueId()));
        Performer performer = null;
        if (req.performerId() != null) {
            performer = performers.findById(req.performerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Performer not found: " + req.performerId()));
        }
        Event saved = events.save(new Event(req.name().trim(), req.description(), req.type(),
            venue, performer, req.startTime(), req.endTime(), req.basePrice()));
        List<Ticket> batch = new ArrayList<>(req.ticketCount());
        for (int i = 1; i <= req.ticketCount(); i++) {
            batch.add(new Ticket(saved, "A-" + i, req.basePrice(), TicketStatus.AVAILABLE));
        }
        tickets.saveAll(batch);
        return eventService.getEventDetails(saved.getId());
    }

    @Transactional
    public List<EventSummaryResponse> createScheduledEvents(CreateScheduledEventsRequest req) {
        var schedule = req.schedule();
        if (schedule.endDate().isBefore(schedule.startDate())) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        if (schedule.startDate().plusDays(60).isBefore(schedule.endDate())) {
            throw new IllegalArgumentException("schedule span must not exceed 60 days");
        }
        List<java.time.LocalTime> times =
            new ArrayList<>(new java.util.TreeSet<>(schedule.showTimes()));
        for (int i = 1; i < times.size(); i++) {
            if (times.get(i).isBefore(times.get(i - 1).plusMinutes(schedule.durationMinutes()))) {
                throw new IllegalArgumentException(
                    "showTimes overlap for duration " + schedule.durationMinutes() + "m");
            }
        }
        Venue venue = venues.findById(req.venueId())
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + req.venueId()));
        Performer performer = null;
        if (req.performerId() != null) {
            performer = performers.findById(req.performerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Performer not found: " + req.performerId()));
        }
        String seriesId = java.util.UUID.randomUUID().toString();
        List<EventSummaryResponse> created = new ArrayList<>();
        for (java.time.LocalDate day = schedule.startDate();
             !day.isAfter(schedule.endDate()); day = day.plusDays(1)) {
            for (java.time.LocalTime time : times) {
                java.time.LocalDateTime start = java.time.LocalDateTime.of(day, time);
                java.time.LocalDateTime end = start.plusMinutes(schedule.durationMinutes());
                String name = req.name().trim() + " (" + day + " " + time + ")";
                Event saved = events.save(new Event(name, req.description(), req.type(),
                    venue, performer, start, end, req.basePrice(), seriesId));
                List<Ticket> batch = new ArrayList<>(req.ticketCount());
                for (int i = 1; i <= req.ticketCount(); i++) {
                    batch.add(new Ticket(saved, "A-" + i, req.basePrice(), TicketStatus.AVAILABLE));
                }
                tickets.saveAll(batch);
                created.add(new EventSummaryResponse(saved.getId(), saved.getName(),
                    saved.getDescription(), saved.getType(), saved.getStartTime(),
                    saved.getEndTime(), saved.getBasePrice(), venue.getName(),
                    performer != null ? performer.getName() : null));
            }
        }
        return created;
    }
}
