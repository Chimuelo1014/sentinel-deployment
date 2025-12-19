package com.sentinel.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration for Auth Service
 * Allows frontend to register/login from localhost:3001
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Read allowed origins from environment variable (set in docker-compose.yml)
        String allowedOriginsEnv = System.getenv("APP_CORS_ALLOWED_ORIGINS");
        String[] allowedOrigins;

        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            allowedOrigins = allowedOriginsEnv.split(",");
        } else {
            // Fallback to localhost for development
            allowedOrigins = new String[] {
                    "http://localhost:3001",
                    "http://localhost:5173",
                    "http://localhost:3000",
                    "http://localhost:5174"
            };
        }

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Auth-Token", "X-Request-ID")
                .allowCredentials(true)
                .maxAge(3600);
    }
}