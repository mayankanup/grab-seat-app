package com.grabseat.controller;

import com.grabseat.dto.*;
import com.grabseat.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/venues")
    @ResponseStatus(HttpStatus.CREATED)
    public VenueDto createVenue(@Valid @RequestBody CreateVenueRequest req) {
        return adminService.createVenue(req.name(), req.location(), req.capacity());
    }

    @PostMapping("/performers")
    @ResponseStatus(HttpStatus.CREATED)
    public PerformerDto createPerformer(@Valid @RequestBody CreatePerformerRequest req) {
        return adminService.createPerformer(req.name(), req.type());
    }

    @PostMapping("/screens")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenDto createScreen(@Valid @RequestBody CreateScreenRequest req) {
        return adminService.createScreen(req.venueId(), req.name(), req.capacity());
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDetailsResponse createEvent(@Valid @RequestBody CreateEventRequest req) {
        return adminService.createEvent(req);
    }

    @PostMapping("/events/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public List<EventSummaryResponse> createScheduledEvents(
            @Valid @RequestBody CreateScheduledEventsRequest req) {
        return adminService.createScheduledEvents(req);
    }

    @GetMapping("/venues")
    public List<VenueDto> listVenues() {
        return adminService.listVenues();
    }

    @GetMapping("/performers")
    public List<PerformerDto> listPerformers() {
        return adminService.listPerformers();
    }

    @GetMapping("/screens")
    public List<ScreenDto> listScreens(
            @RequestParam(required = false) Long venueId) {
        return adminService.listScreens(venueId);
    }

    @GetMapping("/events")
    public Page<EventSummaryResponse> listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return adminService.listEvents(page, pageSize);
    }

    @PutMapping("/venues/{id}")
    public VenueDto updateVenue(@PathVariable Long id,
                                @Valid @RequestBody CreateVenueRequest req) {
        return adminService.updateVenue(id, req.name(), req.location(), req.capacity());
    }

    @PutMapping("/performers/{id}")
    public PerformerDto updatePerformer(@PathVariable Long id,
                                        @Valid @RequestBody CreatePerformerRequest req) {
        return adminService.updatePerformer(id, req.name(), req.type());
    }

    @PutMapping("/screens/{id}")
    public ScreenDto updateScreen(@PathVariable Long id,
                                  @Valid @RequestBody CreateScreenRequest req) {
        return adminService.updateScreen(id, req.name(), req.capacity());
    }

    @PutMapping("/events/{id}")
    public EventDetailsResponse updateEvent(@PathVariable Long id,
                                            @Valid @RequestBody UpdateEventRequest req) {
        return adminService.updateEvent(id, req);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
    }
}
