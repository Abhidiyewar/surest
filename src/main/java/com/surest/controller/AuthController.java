package com.surest.controller;

import com.surest.security.jwt.JwtService;
import com.surest.security.jwt.LoginRequest;
import com.surest.security.jwt.LoginResponse;
import com.surest.service.impl.UserDetailsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserDetailsServiceImpl uds;
    private final JwtService jwtService;

    public AuthController(UserDetailsServiceImpl uds, JwtService jwtService) {
        this.uds = uds;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        var user = uds.authenticate(req.username(), req.password());
        var token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
