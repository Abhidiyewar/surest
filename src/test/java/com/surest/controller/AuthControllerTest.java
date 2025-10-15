package com.surest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.model.Role;
import com.surest.model.User;
import com.surest.security.jwt.JwtAuthFilter;
import com.surest.security.jwt.LoginRequest;
import com.surest.security.jwt.RegisterRequest;
import com.surest.service.AuthService;
import com.surest.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;

    // JwtAuthFilter Mock so the context won't try to create the real @Component
    @MockBean JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /auth/register -> 201 Created with Location and body")
    void register_created_returns201() throws Exception {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID roleId = UUID.fromString("22222222-3333-4444-5555-666666666666");

        RegisterRequest req = new RegisterRequest(null, "alice", "pw12345", roleId);

        Role role = new Role();
        role.setName("ROLE_USER");

        User saved = new User();
        saved.setId(id);
        saved.setUsername("alice");
        saved.setRole(role);

        given(authService.register(req)).willReturn(saved);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/auth/register/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(authService).register(req);
    }

    @Test
    @DisplayName("POST /auth/register -> 400 when @Valid fails")
    void register_validationError_returns400() throws Exception {
        RegisterRequest invalid = new RegisterRequest(null, "", "", null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login -> 200 OK with token")
    void login_ok_returnsToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("bob", "pw");

        var userDetails = new org.springframework.security.core.userdetails.User(
                "bob", "{noop}pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = "header.payload.signature";

        given(authService.authenticate(loginRequest)).willReturn(userDetails);
        given(jwtService.generateToken(userDetails)).willReturn(token);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    @DisplayName("POST /auth/login -> 400 when @Valid fails")
    void login_validationError_returns400() throws Exception {
        LoginRequest invalid = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
