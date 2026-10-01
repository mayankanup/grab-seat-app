package com.grabseat.controller;

import com.grabseat.security.JwtService;
import com.grabseat.security.SecurityConfig;
import com.grabseat.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AdminService adminService;

    @MockBean
    JwtService jwtService;

    private static final String EVENT_JSON = "{\"name\":\"New Night Show\","
        + "\"description\":\"Late comedy\",\"type\":\"COMEDY\",\"venueId\":3,"
        + "\"performerId\":4,\"startTime\":\"2026-11-01T20:00:00\","
        + "\"endTime\":\"2026-11-01T22:00:00\",\"basePrice\":499.00,\"ticketCount\":5}";

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void adminCanCreateEvent() throws Exception {
        when(adminService.createEvent(any())).thenReturn(null);

        mvc.perform(post("/api/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EVENT_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void anonymousCannotCreateEvent() throws Exception {
        mvc.perform(post("/api/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EVENT_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "7", roles = "USER")
    void userCannotCreateEvent() throws Exception {
        mvc.perform(post("/api/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EVENT_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void adminCanCreateVenue() throws Exception {
        mvc.perform(post("/api/admin/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hall\",\"location\":\"Town\",\"capacity\":100}"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void adminCanCreateScreen() throws Exception {
        mvc.perform(post("/api/admin/screens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueId\":3,\"name\":\"IMAX\",\"capacity\":80}"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "7", roles = "USER")
    void userCannotCreateScreen() throws Exception {
        mvc.perform(post("/api/admin/screens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueId\":3,\"name\":\"IMAX\",\"capacity\":80}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void adminCanCreateSchedule() throws Exception {
        when(adminService.createScheduledEvents(any())).thenReturn(List.of());

        mvc.perform(post("/api/admin/events/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Morning Laughs\",\"type\":\"COMEDY\",\"venueId\":3,"
                    + "\"basePrice\":299.00,\"ticketCount\":2,\"schedule\":{"
                    + "\"startDate\":\"2026-11-03\",\"endDate\":\"2026-11-04\","
                    + "\"showTimes\":[\"08:00\",\"10:00\"],\"durationMinutes\":90}}"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "7", roles = "USER")
    void userCannotCreateSchedule() throws Exception {
        mvc.perform(post("/api/admin/events/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void adminCanListVenues() throws Exception {
        when(adminService.listVenues()).thenReturn(List.of());

        mvc.perform(get("/api/admin/venues"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void adminCanUpdateEvent() throws Exception {
        mvc.perform(put("/api/admin/events/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Renamed\",\"type\":\"COMEDY\",\"venueId\":3,"
                    + "\"startTime\":\"2026-11-01T20:00:00\",\"basePrice\":499.00}"))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotListVenues() throws Exception {
        mvc.perform(get("/api/admin/venues"))
            .andExpect(status().isUnauthorized());
    }
}
