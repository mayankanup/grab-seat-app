package com.grabseat.controller;

import com.grabseat.dto.ConfirmBookingRequest;
import com.grabseat.dto.ConfirmBookingResponse;
import com.grabseat.dto.ReserveTicketsRequest;
import com.grabseat.dto.ReserveTicketsResponse;
import com.grabseat.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public ReserveTicketsResponse reserve(@Valid @RequestBody ReserveTicketsRequest req,
                                          Authentication auth) {
        return bookingService.reserve(req.ticketIds(), auth.getName());
    }

    @PostMapping("/confirm")
    public ConfirmBookingResponse confirm(@Valid @RequestBody ConfirmBookingRequest req,
                                          Authentication auth) {
        return bookingService.confirm(req.bookingId(), auth.getName(), req.paymentToken());
    }
}
