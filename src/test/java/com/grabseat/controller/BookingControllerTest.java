package com.grabseat.controller;

import com.grabseat.dto.BookingResponse;
import com.grabseat.exception.TicketNotAvailableException;
import com.grabseat.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    BookingService bookingService;

    @Test
    void reserveReturns201() throws Exception {
        when(bookingService.reserve(eq(10L), eq("user-1"))).thenReturn(
            new BookingResponse(1L, 10L, 5L, "A-1", new BigDecimal("349.00"),
                "RESERVED", "RESERVED", "user-1"));

        mvc.perform(post("/bookings/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketId\":10,\"userId\":\"user-1\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ticketStatus").value("RESERVED"))
            .andExpect(jsonPath("$.seatNumber").value("A-1"));
    }

    @Test
    void reserveConflictReturns409() throws Exception {
        when(bookingService.reserve(eq(10L), any()))
            .thenThrow(new TicketNotAvailableException("Ticket 10 is not available"));

        mvc.perform(post("/bookings/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketId\":10,\"userId\":\"user-2\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void confirmReturns200() throws Exception {
        when(bookingService.confirm(eq(10L), eq("user-1"), eq("tok_test"))).thenReturn(
            new BookingResponse(1L, 10L, 5L, "A-1", new BigDecimal("349.00"),
                "BOOKED", "CONFIRMED", "user-1"));

        mvc.perform(post("/bookings/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketId\":10,\"userId\":\"user-1\",\"paymentToken\":\"tok_test\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticketStatus").value("BOOKED"))
            .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));
    }
}
