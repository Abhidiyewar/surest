package com.surest.controller;

import com.surest.security.jwt.JwtAuthFilter;
import com.surest.service.JwtService;
import com.surest.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for JwtAuthFilter (no Spring context).
 * Uses user-provided JWT in Authorization header.
 */
class JwtAuthFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);

    private static final String VALID_HEADER =
            "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmhpIiwiaWF0IjoxNzYwNDQ0ODgwLCJleHAiOjE3NjA0NDg0ODB9.lOWxtlTkZ-ilMm0Hpgy_8I4wTpZrSUa-XRIXCSE6DeU";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Sets authentication when bearer token is valid")
    void setsAuthentication_whenTokenValid() throws ServletException, IOException {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/secured");
        req.addHeader("Authorization", VALID_HEADER);
        MockHttpServletResponse res = new MockHttpServletResponse();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("abhi").password("abhi123").authorities("ROLE_USER").build();

        when(jwtService.extractUsername(anyString())).thenReturn("abhi");
        when(userDetailsService.loadUserByUsername("abhi")).thenReturn(userDetails);
        when(jwtService.validateToken(anyString(), eq(userDetails))).thenReturn(true);

        // when
        filter.doFilter(req, res, passthroughChain());

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("abhi");
        verify(jwtService).extractUsername(anyString());
        verify(jwtService).validateToken(anyString(), eq(userDetails));
        verify(userDetailsService).loadUserByUsername("abhi");
    }

    @Test
    @DisplayName("Does not authenticate when header missing")
    void noAuth_whenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/secured");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, passthroughChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Does not authenticate when token validation fails")
    void noAuth_whenValidationFails() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/secured");
        req.addHeader("Authorization", VALID_HEADER);
        MockHttpServletResponse res = new MockHttpServletResponse();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("abhi").password("abhi123").authorities("ROLE_USER").build();

        when(jwtService.extractUsername(anyString())).thenReturn("abhi");
        when(userDetailsService.loadUserByUsername("abhi")).thenReturn(userDetails);
        when(jwtService.validateToken(anyString(), eq(userDetails))).thenReturn(false);

        filter.doFilter(req, res, passthroughChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService).extractUsername(anyString());
        verify(jwtService).validateToken(anyString(), eq(userDetails));
        verify(userDetailsService).loadUserByUsername("abhi");
    }

    private static FilterChain passthroughChain() {
        return (request, response) -> { /* no-op */ };
    }
}
