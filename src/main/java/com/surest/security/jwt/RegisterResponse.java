package com.surest.security.jwt;

import java.util.UUID;

public record RegisterResponse(UUID id, String username, String role,String msg) {}
