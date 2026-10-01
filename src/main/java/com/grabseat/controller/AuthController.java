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
        UserAccount user =
            userService.register(req.login(), req.password(), req.fullName(), req.email());
        return tokenFor(user);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return tokenFor(userService.authenticate(req.login(), req.password()));
    }

    private TokenResponse tokenFor(UserAccount user) {
        return new TokenResponse(user.getId(), user.getLogin(), user.getFullName(),
            user.getEmail(), user.getRole().name(),
            jwtService.issue(user.getId(), user.getLogin(), user.getRole().name()));
    }
}
