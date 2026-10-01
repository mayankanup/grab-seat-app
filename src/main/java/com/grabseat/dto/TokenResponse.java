package com.grabseat.dto;

public record TokenResponse(
    Long userId,
    String login,
    String fullName,
    String email,
    String role,
    String token
) {
}
