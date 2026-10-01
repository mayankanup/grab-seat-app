package com.grabseat.controller;

import com.grabseat.dto.EventDetailsResponse;
import com.grabseat.dto.EventSummaryResponse;
import com.grabseat.search.SearchService;
import com.grabseat.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final SearchService searchService;

    public EventController(EventService eventService, SearchService searchService) {
        this.eventService = eventService;
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public Page<EventSummaryResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return searchService.search(keyword, start, end, page, pageSize);
    }

    @GetMapping("/{eventId}")
    public EventDetailsResponse view(@PathVariable Long eventId) {
        return eventService.getEventDetails(eventId);
    }
}
