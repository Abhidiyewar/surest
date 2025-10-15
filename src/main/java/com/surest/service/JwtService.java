package com.surest.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        var now = new Date();
        var expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date exp = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            boolean expired = exp.before(new Date());
            if (expired) {
                log.warn("JWT is expired");
            }
            return expired;
        } catch (JwtException e) {
            log.warn("Failed to parse JWT for expiration check: {}", e.getClass().getSimpleName());
            return true;
        }
    }

    public boolean validateToken(String token, UserDetails user) {
        try {
            final String username = extractUsername(token);
            boolean valid = (username != null && username.equals(user.getUsername()) && !isTokenExpired(token));
            if (valid) {
                log.info("JWT validated for username='{}'", user.getUsername());
            } else {
                log.warn("JWT validation failed for username='{}'", user.getUsername());
            }
            return valid;
        } catch (JwtException ex) {
            log.warn("JWT validation error: {}", ex.getClass().getSimpleName());
            return false;
        }
    }
}
