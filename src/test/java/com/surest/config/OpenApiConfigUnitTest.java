package com.surest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigUnitTest {

    @Test
    @DisplayName("CustomOpenAPI Should Configure Bearer JwtSecurity")
    void openApiForJWTAuthentication() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());

        String schemeName = "bearerAuth";
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get(schemeName);
        assertNotNull(scheme, "Security scheme 'bearerAuth' must be present");
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());

        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());

        boolean hasRequirement = openAPI.getSecurity().stream()
                .map(SecurityRequirement::keySet)
                .anyMatch(keys -> keys.contains(schemeName));
        assertTrue(hasRequirement, "Security requirement must include 'bearerAuth'");
    }
}
