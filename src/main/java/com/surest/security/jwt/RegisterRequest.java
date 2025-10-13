package com.surest.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterRequest(
        @NotNull UUID id,
        @NotBlank String username,
        @NotBlank String password,
        @NotNull UUID roleId
) {}
