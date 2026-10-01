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
        String token = service.issue(7L, "anup");

        assertThat(service.parseUserId(token)).isEqualTo(7L);
    }

    @Test
    void issuedTokenCarriesUserIdAndLoginClaims() {
        String token = service.issue(7L, "anup");
        String payload = new String(
            java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]),
            java.nio.charset.StandardCharsets.UTF_8);

        assertThat(payload).contains("\"userId\":7");
        assertThat(payload).contains("\"login\":\"anup\"");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.issue(7L, "anup");

        assertThatThrownBy(() -> service.parseUserId(token + "tampered"))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherSecretIsRejected() {
        String token = service.issue(7L, "anup");
        JwtService other = new JwtService("another-secret-for-tests-32bytes!!!", 3600000);

        assertThatThrownBy(() -> other.parseUserId(token))
            .isInstanceOf(JwtException.class);
    }
}
