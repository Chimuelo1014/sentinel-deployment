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
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3001", // Frontend Docker
                        "http://localhost:5173", // Vite dev server
                        "http://localhost:3000", // Alternative
                        "http://localhost:5174" // Alternative Vite
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Auth-Token", "X-Request-ID")
                .allowCredentials(true)
                .maxAge(3600);
    }
}