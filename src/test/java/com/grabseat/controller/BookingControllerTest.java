package com.grabseat.controller;

import com.grabseat.dto.ConfirmBookingResponse;
import com.grabseat.dto.ReserveTicketsResponse;
import com.grabseat.exception.TicketNotAvailableException;
import com.grabseat.security.JwtService;
import com.grabseat.security.SecurityConfig;
import com.grabseat.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    BookingService bookingService;

    @MockBean
    JwtService jwtService;

    private ReserveTicketsResponse reserveResponse() {
        return new ReserveTicketsResponse(1L, "RESERVED", "user-1", List.of(
            new ReserveTicketsResponse.BookedTicketDto(10L, 5L, "A-1",
                new BigDecimal("349.00"), "RESERVED"),
            new ReserveTicketsResponse.BookedTicketDto(11L, 5L, "A-2",
                new BigDecimal("349.00"), "RESERVED")));
    }

    @Test
    void reserveReturns201WithTickets() throws Exception {
        when(jwtService.parseUserId("test-token")).thenReturn("user-1");
        when(bookingService.reserve(eq(List.of(10L, 11L)), eq("user-1")))
            .thenReturn(reserveResponse());

        mvc.perform(post("/bookings/reserve")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketIds\":[10,11]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.bookingStatus").value("RESERVED"))
            .andExpect(jsonPath("$.tickets[0].seatNumber").value("A-1"))
            .andExpect(jsonPath("$.tickets[1].seatNumber").value("A-2"));
    }

    @Test
    void reserveConflictReturns409() throws Exception {
        when(jwtService.parseUserId("test-token")).thenReturn("user-2");
        when(bookingService.reserve(eq(List.of(10L)), any()))
            .thenThrow(new TicketNotAvailableException("Ticket 10 is not available"));

        mvc.perform(post("/bookings/reserve")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketIds\":[10]}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void reserveWithoutTokenReturns401() throws Exception {
        mvc.perform(post("/bookings/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketIds\":[10]}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void confirmReturns200() throws Exception {
        when(jwtService.parseUserId("test-token")).thenReturn("user-1");
        when(bookingService.confirm(eq(1L), eq("user-1"), eq("tok_test"))).thenReturn(
            new ConfirmBookingResponse(1L, "CONFIRMED", "user-1", List.of(
                new ConfirmBookingResponse.BookedTicketDto(10L, 5L, "A-1",
                    new BigDecimal("349.00"), "BOOKED"))));

        mvc.perform(post("/bookings/confirm")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingId\":1,\"paymentToken\":\"tok_test\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"))
            .andExpect(jsonPath("$.tickets[0].ticketStatus").value("BOOKED"));
    }
}
