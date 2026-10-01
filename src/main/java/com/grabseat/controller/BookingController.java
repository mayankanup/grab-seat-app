package com.grabseat.controller;

import com.grabseat.dto.BookingResponse;
import com.grabseat.dto.ConfirmRequest;
import com.grabseat.dto.ReserveRequest;
import com.grabseat.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public BookingResponse reserve(@Valid @RequestBody ReserveRequest req) {
        return bookingService.reserve(req.ticketId(), req.userId());
    }

    @PostMapping("/confirm")
    public BookingResponse confirm(@Valid @RequestBody ConfirmRequest req) {
        return bookingService.confirm(req.ticketId(), req.userId(), req.paymentToken());
    }
}
