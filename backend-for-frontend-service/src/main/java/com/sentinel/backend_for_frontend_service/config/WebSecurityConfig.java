package com.sentinel.backend_for_frontend_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for BFF service.
 * Enables CORS and configures security for frontend access.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // CORS disabled - handled by Kong API Gateway to avoid duplicate headers
                                .cors(cors -> cors.disable())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/actuator/**").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                                                "/v3/api-docs/**")
                                                .permitAll()
                                                .requestMatchers("/api/bff/**").permitAll() // BFF endpoints - JWT
                                                                                            // validated by Kong
                                                .anyRequest().permitAll());

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Allow all origins for development (configure specific origins for production)
                configuration.setAllowedOriginPatterns(List.of("*"));

                // Allowed HTTP methods
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

                // Allowed headers
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With",
                                "X-Tenant-Id",
                                "X-Request-ID",
                                "Cache-Control"));

                // Exposed headers (visible to frontend)
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "X-Total-Count",
                                "X-Request-ID",
                                "Content-Disposition"));

                // Allow credentials (cookies, authorization headers)
                configuration.setAllowCredentials(true);

                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
