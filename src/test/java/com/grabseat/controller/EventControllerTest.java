package com.grabseat.controller;

import com.grabseat.dto.*;
import com.grabseat.model.EventType;
import com.grabseat.model.TicketStatus;
import com.grabseat.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventService eventService;

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
}
