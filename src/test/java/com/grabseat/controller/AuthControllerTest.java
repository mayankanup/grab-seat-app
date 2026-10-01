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

    @Test
    void registerReturns201WithToken() throws Exception {
        when(userService.register(eq("anup"), eq("password123")))
            .thenReturn(new UserAccount("anup", "hash"));
        when(jwtService.issue(eq("anup"))).thenReturn("jwt-token");

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"anup\",\"password\":\"password123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("anup"))
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerDuplicateReturns409() throws Exception {
        when(userService.register(eq("anup"), eq("password123")))
            .thenThrow(new ConflictException("User already exists: anup"));

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"anup\",\"password\":\"password123\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void loginReturns200WithToken() throws Exception {
        when(userService.authenticate(eq("anup"), eq("password123")))
            .thenReturn(new UserAccount("anup", "hash"));
        when(jwtService.issue(eq("anup"))).thenReturn("jwt-token");

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"anup\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginBadCredentialsReturns401() throws Exception {
        when(userService.authenticate(eq("anup"), eq("wrongpass1")))
            .thenThrow(new UnauthorizedException("Invalid credentials"));

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"anup\",\"password\":\"wrongpass1\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }
}
