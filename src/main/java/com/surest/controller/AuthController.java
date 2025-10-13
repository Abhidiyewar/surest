package com.surest.controller;

import com.surest.model.User;
import com.surest.security.jwt.LoginRequest;
import com.surest.security.jwt.LoginResponse;
import com.surest.security.jwt.RegisterRequest;
import com.surest.security.jwt.RegisterResponse;
import com.surest.service.JwtService;
import com.surest.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest req) {
        // AuthService.register(...) throws IllegalArgumentException if invalid (handled by ControllerAdvice)
        User saved = authService.register(req);

        RegisterResponse body = new RegisterResponse(saved.getId(), saved.getUsername(), saved.getRole().getName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(body); // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        // AuthService.authenticate(...) throws UsernameNotFoundException/BadCredentialsException on failure
        var ud = authService.authenticate(req);
        var token = jwtService.generateToken(ud);
        return ResponseEntity.ok(new LoginResponse(token)); // 200 OK with typed body
    }
}
