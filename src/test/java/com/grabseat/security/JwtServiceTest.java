package com.grabseat.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-for-unit-tests-32bytes!!";

    private final JwtService service = new JwtService(SECRET, 3600000);

    @Test
    void issuedTokenParsesBackToUserId() {
        String token = service.issue("user-1");

        assertThat(service.parseUserId(token)).isEqualTo("user-1");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.issue("user-1");

        assertThatThrownBy(() -> service.parseUserId(token + "tampered"))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherSecretIsRejected() {
        String token = service.issue("user-1");
        JwtService other = new JwtService("another-secret-for-tests-32bytes!!!", 3600000);

        assertThatThrownBy(() -> other.parseUserId(token))
            .isInstanceOf(JwtException.class);
    }
}
