package com.grabseat.service;

import com.grabseat.dto.*;
import com.grabseat.exception.ConflictException;
import com.grabseat.exception.ResourceNotFoundException;
import com.grabseat.model.*;
import com.grabseat.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final VenueRepository venues;
    private final PerformerRepository performers;
    private final EventRepository events;
    private final TicketRepository tickets;
    private final ScreenRepository screens;
    private final EventService eventService;

    public AdminService(VenueRepository venues, PerformerRepository performers,
                        EventRepository events, TicketRepository tickets,
                        ScreenRepository screens, EventService eventService) {
        this.venues = venues;
        this.performers = performers;
        this.events = events;
        this.tickets = tickets;
        this.screens = screens;
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
                e.getPerformer() != null ? e.getPerformer().getName() : null,
                e.getScreen() != null ? e.getScreen().getName() : null));
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
    public ScreenDto createScreen(Long venueId, String name, Integer capacity) {
        Venue venue = venues.findById(venueId)
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + venueId));
        Screen saved = screens.save(new Screen(venue, name.trim(), capacity));
        return toScreenDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ScreenDto> listScreens(Long venueId) {
        List<Screen> all = venueId == null
            ? screens.findAll(Sort.by("id"))
            : screens.findByVenueIdOrderById(venueId);
        return all.stream().map(this::toScreenDto).toList();
    }

    @Transactional
    public ScreenDto updateScreen(Long id, String name, Integer capacity) {
        Screen screen = screens.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + id));
        screen.update(name.trim(), capacity);
        return toScreenDto(screen);
    }

    private ScreenDto toScreenDto(Screen screen) {
        return new ScreenDto(screen.getId(), screen.getVenue().getId(),
            screen.getVenue().getName(), screen.getName(), screen.getCapacity());
    }

    private Screen resolveScreen(Long screenId, Venue venue) {
        if (screenId == null) {
            return null;
        }
        Screen screen = screens.findById(screenId)
            .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));
        if (!sameVenue(screen.getVenue(), venue)) {
            throw new IllegalArgumentException(
                "Screen " + screenId + " does not belong to venue " + venue.getId());
        }
        return screen;
    }

    private boolean sameVenue(Venue a, Venue b) {
        if (a == b) {
            return true;
        }
        return a.getId() != null && a.getId().equals(b.getId());
    }

    private int ticketCountFor(Integer requested, Screen screen) {
        if (requested != null) {
            return requested;
        }
        if (screen != null) {
            return screen.getCapacity();
        }
        throw new IllegalArgumentException("ticketCount is required when no screen is given");
    }

    private void rejectScreenClash(Screen screen, LocalDateTime start, LocalDateTime end,
                                   Long ignoreEventId) {
        if (screen == null || end == null) {
            return;
        }
        Specification<Event> spec = (root, query, cb) -> cb.and(
            cb.equal(root.get("screen").get("id"), screen.getId()),
            cb.lessThan(root.get("startTime"), end),
            cb.or(
                cb.and(cb.isNull(root.get("endTime")),
                    cb.greaterThanOrEqualTo(root.get("startTime"), start)),
                cb.and(cb.isNotNull(root.get("endTime")),
                    cb.greaterThan(root.get("endTime"), start))));
        if (ignoreEventId != null) {
            Specification<Event> self = (root, query, cb) -> cb.notEqual(root.get("id"), ignoreEventId);
            spec = spec.and(self);
        }
        if (events.count(spec) > 0) {
            throw new IllegalArgumentException("Screen " + screen.getName()
                + " already has a show overlapping " + start + "-" + end);
        }
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
        Screen screen = resolveScreen(req.screenId(), venue);
        rejectScreenClash(screen, req.startTime(), req.endTime(), event.getId());
        event.update(req.name().trim(), req.description(), req.type(), venue, performer,
            req.startTime(), req.endTime(), req.basePrice(), screen);
        return eventService.getEventDetails(event.getId());
    }

    /**
     * Drops an event and its seats. Refused while any seat is reserved or
     * booked: those rows are referenced by a booking a customer has paid for,
     * so cancelling the show is a business decision, not a cleanup. The CDC
     * delete event removes the Elasticsearch document.
     */
    @Transactional
    public void deleteEvent(Long id) {
        Event event = events.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        List<Ticket> seats = tickets.findByEventId(id);
        long taken = seats.stream().filter(t -> t.getStatus() != TicketStatus.AVAILABLE).count();
        if (taken > 0) {
            throw new ConflictException("Cannot delete event " + id + ": " + taken
                + " seat(s) are reserved or booked");
        }
        tickets.deleteAll(seats);
        events.delete(event);
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
        Screen screen = resolveScreen(req.screenId(), venue);
        rejectScreenClash(screen, req.startTime(), req.endTime(), null);
        Event saved = events.save(new Event(req.name().trim(), req.description(), req.type(),
            venue, performer, req.startTime(), req.endTime(), req.basePrice(), null, screen));
        int count = ticketCountFor(req.ticketCount(), screen);
        List<Ticket> batch = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
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
        Screen screen = resolveScreen(req.screenId(), venue);
        int count = ticketCountFor(req.ticketCount(), screen);
        String seriesId = java.util.UUID.randomUUID().toString();
        List<EventSummaryResponse> created = new ArrayList<>();
        for (java.time.LocalDate day = schedule.startDate();
             !day.isAfter(schedule.endDate()); day = day.plusDays(1)) {
            for (java.time.LocalTime time : times) {
                java.time.LocalDateTime start = java.time.LocalDateTime.of(day, time);
                java.time.LocalDateTime end = start.plusMinutes(schedule.durationMinutes());
                rejectScreenClash(screen, start, end, null);
                String name = req.name().trim() + " (" + day + " " + time + ")";
                Event saved = events.save(new Event(name, req.description(), req.type(),
                    venue, performer, start, end, req.basePrice(), seriesId, screen));
                List<Ticket> batch = new ArrayList<>(count);
                for (int i = 1; i <= count; i++) {
                    batch.add(new Ticket(saved, "A-" + i, req.basePrice(), TicketStatus.AVAILABLE));
                }
                tickets.saveAll(batch);
                created.add(new EventSummaryResponse(saved.getId(), saved.getName(),
                    saved.getDescription(), saved.getType(), saved.getStartTime(),
                    saved.getEndTime(), saved.getBasePrice(), venue.getName(),
                    performer != null ? performer.getName() : null,
                    screen != null ? screen.getName() : null));
            }
        }
        return created;
    }
}
