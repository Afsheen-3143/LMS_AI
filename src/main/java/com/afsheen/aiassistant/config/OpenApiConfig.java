package com.afsheen.aiassistant.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Declares the bearer-JWT scheme to springdoc so Swagger UI shows an
 * "Authorize" button. Without this, pasting a token from
 * GET /api/dev/token/{id} has nowhere to go in the UI - every request would
 * still be sent unauthenticated regardless of persistAuthorization in
 * application.yml.
 */
@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
