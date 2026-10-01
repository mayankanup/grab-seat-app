package com.grabseat.controller;

import com.grabseat.dto.*;
import com.grabseat.model.EventType;
import com.grabseat.model.TicketStatus;
import com.grabseat.security.JwtService;
import com.grabseat.security.SecurityConfig;
import com.grabseat.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import(SecurityConfig.class)
class EventControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventService eventService;

    @MockBean
    JwtService jwtService;

    @Test
    void getEventReturns200WithDetails() throws Exception {
        var res = new EventDetailsResponse(1L, "Dune Evening Show", "IMAX", EventType.MOVIE,
            LocalDateTime.parse("2026-10-02T18:00:00"), LocalDateTime.parse("2026-10-02T21:00:00"),
            new BigDecimal("349.00"),
            new VenueDto(1L, "PVR Downtown Cinema", "Downtown Mall", 120),
            new PerformerDto(1L, "Dune Cast", "MOVIE_CAST"),
            List.of(new TicketDto(1L, "A-1", new BigDecimal("349.00"), TicketStatus.AVAILABLE)));
        when(eventService.getEventDetails(1L)).thenReturn(res);

        mvc.perform(get("/events/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Dune Evening Show"))
            .andExpect(jsonPath("$.venue.name").value("PVR Downtown Cinema"))
            .andExpect(jsonPath("$.tickets[0].seatNumber").value("A-1"));
    }

    @Test
    void searchReturns200WithPage() throws Exception {
        var summary = new EventSummaryResponse(1L, "Dune Evening Show", "IMAX", EventType.MOVIE,
            LocalDateTime.parse("2026-10-02T18:00:00"), LocalDateTime.parse("2026-10-02T21:00:00"),
            new BigDecimal("349.00"), "PVR Downtown Cinema", "Dune Cast");
        when(eventService.search(any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(summary)));

        mvc.perform(get("/events/search").param("keyword", "dune"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Dune Evening Show"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }
}
