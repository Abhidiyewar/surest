package com.surest.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static JwtService jwtService(String secret, long expirationMs) {
        JwtService jwtService = new JwtService();
        setField(jwtService, "secret", secret);
        setField(jwtService, "expirationMs", expirationMs);
        return jwtService;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field declaredField = target.getClass().getDeclaredField(name);
            declaredField.setAccessible(true);
            declaredField.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String SECRET_1 = "01234567890123456789012345678901"; // 32 bytes (HS256 ok)
    private static final String SECRET_2 = "ABCDEFGHABCDEFGHABCDEFGHABCDEFGH"; // different 32 bytes

    @Test
    @DisplayName("generateToken -> extractUsername/validateToken OK; not expired")
    void generate_and_validate_ok() {
        var jwtService = jwtService(SECRET_1, 60_000L);
        var userDetails = User.withUsername("alice").password("alice123").authorities("ROLE_USER").build();

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenExpired(token)).isFalse();
        assertThat(jwtService.validateToken(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("generateToken with negative expiry -> token is expired")
    void expired_token() {
        var svc = jwtService(SECRET_1, -1_000L);
        var userDetails = User.withUsername("bob").password("bob123").authorities("ROLE_USER").build();

        String token = svc.generateToken(userDetails);

        assertThat(svc.isTokenExpired(token)).isTrue();
        assertThat(svc.validateToken(token, userDetails)).isFalse();
    }

    @Test
    @DisplayName("extractUsername throws on invalid signature (wrong secret)")
    void invalid_signature_extract_throws() {

        var signer = jwtService(SECRET_1, 60_000L);
        var token = signer.generateToken(User.withUsername("carol").password("carol123").authorities("ROLE_ADMIN").build());
        var verifier = jwtService(SECRET_2, 60_000L);

        assertThrows(JwtException.class, () -> verifier.extractUsername(token));
        assertThat(verifier.validateToken(token, User.withUsername("carol").password("carol123").authorities("ROLE_ADMIN").build())).isFalse();
    }

    @Test
    @DisplayName("malformed token -> extract throws; isTokenExpired true; validateToken false")
    void malformed_token_behaviour() {
        var svc = jwtService(SECRET_1, 60_000L);
        String bad = "not.a.jwt";

        assertThrows(JwtException.class, () -> svc.extractUsername(bad));
        assertThat(svc.isTokenExpired(bad)).isTrue();
        assertThat(svc.validateToken(bad, User.withUsername("dave").password("x").authorities("R").build())).isFalse();
    }
}
