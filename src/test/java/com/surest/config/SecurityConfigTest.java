package com.surest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.controller.AuthController;
import com.surest.security.SecurityConfig;
import com.surest.security.jwt.JwtAuthFilter;
import com.surest.security.jwt.LoginRequest;
import com.surest.service.AuthService;
import com.surest.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - Uses real AuthController
 * - Mocks AuthService/JwtService
 * - Replaces real JwtAuthFilter with a test no-op bean to avoid pulling UserDetailsServiceImpl
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class // exclude the real @Component filter
        )
)
@Import({SecurityConfig.class, SecurityConfigTest.TestBeans.class})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;

    /** No-op filter so SecurityConfig can wire .addFilterBefore(...) without needing real services. */
    static class NoopJwtAuthFilter extends JwtAuthFilter {
        NoopJwtAuthFilter() { super(null, null); }
        @Override
        protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {

            req.setAttribute("jwtFilter", true);
            chain.doFilter(req, res);
        }
    }

    @TestConfiguration
    static class TestBeans {
        @Bean @Primary
        JwtAuthFilter jwtAuthFilter() { return new NoopJwtAuthFilter(); }
    }


    @Test
    @DisplayName("Permit-all: /auth/login is accessible without auth")
    void permitAll_AuthLogin() throws Exception {

        var loginRequest = new LoginRequest("bob", "bob123");
        var userDetails = new org.springframework.security.core.userdetails.User(
                "bob", "{noop}bob123", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(authService.authenticate(loginRequest)).willReturn(userDetails);
        given(jwtService.generateToken(userDetails)).willReturn("eyJLCJleHAiOjE3NjA1NTEzNDB9");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJLCJleHAiOjE3NjA1NTEzNDB9"));
    }

    @Test
    @DisplayName("CSRF disabled: POST /auth/login without CSRF token succeeds")
    void csrfDisabled_onAuthPaths() throws Exception {

        var loginRequest = new LoginRequest("bob", "pw");
        var userDetails = new org.springframework.security.core.userdetails.User(
                "bob", "{noop}pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(authService.authenticate(loginRequest)).willReturn(userDetails);
        given(jwtService.generateToken(userDetails)).willReturn("t");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unknown non-/auth path is not permitted (will not return 200)")
    void nonAuthPath_notPermitted_expectNotOk() throws Exception {

        mockMvc.perform(get("/secure/ping"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("BCryptPasswordEncoder bean exists and works")
    void passwordEncoderBean() {
        String rawPassword = "secret123";
        String hashPassword = passwordEncoder.encode(rawPassword);
        assert passwordEncoder.matches(rawPassword, hashPassword);
    }
}
