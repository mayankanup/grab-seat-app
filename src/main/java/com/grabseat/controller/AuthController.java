package com.grabseat.controller;

import com.grabseat.dto.LoginRequest;
import com.grabseat.dto.RegisterRequest;
import com.grabseat.dto.TokenResponse;
import com.grabseat.model.UserAccount;
import com.grabseat.security.JwtService;
import com.grabseat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest req) {
        UserAccount user = userService.register(req.userId(), req.password());
        return new TokenResponse(user.getUsername(), jwtService.issue(user.getUsername()));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        UserAccount user = userService.authenticate(req.userId(), req.password());
        return new TokenResponse(user.getUsername(), jwtService.issue(user.getUsername()));
    }
}
