package com.sentinel.backend_for_frontend_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration for BFF Service
 * Allows frontend (port 3001, 5173) to access all BFF endpoints
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3001", // Frontend Docker
                        "http://localhost:5173", // Vite dev server
                        "http://localhost:3000", // Alternative
                        "http://localhost:5174" // Alternative Vite
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Total-Count", "X-Request-ID")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
