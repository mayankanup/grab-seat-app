package com.grabseat.controller;

import com.grabseat.dto.EventDetailsResponse;
import com.grabseat.service.EventService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{eventId}")
    public EventDetailsResponse view(@PathVariable Long eventId) {
        return eventService.getEventDetails(eventId);
    }
}
