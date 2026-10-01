package com.grabseat.controller;

import com.grabseat.exception.ConflictException;
import com.grabseat.exception.UnauthorizedException;
import com.grabseat.model.UserAccount;
import com.grabseat.security.JwtService;
import com.grabseat.security.SecurityConfig;
import com.grabseat.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    @MockBean
    JwtService jwtService;

    private static final String REGISTER_JSON = "{\"login\":\"anup\",\"password\":\"password123\","
        + "\"fullName\":\"Anup Kumar\",\"email\":\"anup@example.com\"}";

    private UserAccount account() {
        return new UserAccount("anup", "hash", "Anup Kumar", "anup@example.com");
    }

    @Test
    void registerReturns201WithToken() throws Exception {
        when(userService.register(eq("anup"), eq("password123"), eq("Anup Kumar"),
            eq("anup@example.com"))).thenReturn(account());
        when(jwtService.issue(any(), eq("anup"))).thenReturn("jwt-token");

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTER_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.login").value("anup"))
            .andExpect(jsonPath("$.fullName").value("Anup Kumar"))
            .andExpect(jsonPath("$.email").value("anup@example.com"))
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerDuplicateReturns409() throws Exception {
        when(userService.register(eq("anup"), eq("password123"), eq("Anup Kumar"),
            eq("anup@example.com")))
            .thenThrow(new ConflictException("Login already taken: anup"));

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTER_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void loginReturns200WithToken() throws Exception {
        when(userService.authenticate(eq("anup"), eq("password123"))).thenReturn(account());
        when(jwtService.issue(any(), eq("anup"))).thenReturn("jwt-token");

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"anup\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("anup"))
            .andExpect(jsonPath("$.fullName").value("Anup Kumar"))
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginBadCredentialsReturns401() throws Exception {
        when(userService.authenticate(eq("anup"), eq("wrongpass1")))
            .thenThrow(new UnauthorizedException("Invalid credentials"));

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"anup\",\"password\":\"wrongpass1\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }
}
