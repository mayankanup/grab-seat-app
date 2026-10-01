package com.grabseat.controller;

import com.grabseat.dto.LoginRequest;
import com.grabseat.dto.TokenResponse;
import com.grabseat.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Dev-only token minting. A real user store + password check arrives with the auth story;
 * until then this issues a JWT for any given userId so bookings can carry a principal.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return new TokenResponse(req.userId(), jwtService.issue(req.userId()));
    }
}
