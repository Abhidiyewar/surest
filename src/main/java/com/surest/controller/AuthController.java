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
    public ResponseEntity<RegisterResponse> registerMembers(@RequestBody @Valid RegisterRequest registerRequest) {
        User saveUser = authService.register(registerRequest);
        RegisterResponse body = new RegisterResponse(saveUser.getId(),
                saveUser.getUsername(), saveUser.getRole().getName(),
                "User Registered Successfully!");
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saveUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginMembers(@RequestBody @Valid LoginRequest loginRequest) {
        var userDetails = authService.authenticate(loginRequest);
        var generatedToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponse(generatedToken));
    }
}
