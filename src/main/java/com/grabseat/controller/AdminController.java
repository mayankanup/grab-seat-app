package com.grabseat.controller;

import com.grabseat.dto.*;
import com.grabseat.service.AdminService;
import jakarta.validation.Valid;
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
}
