package com.sentinel.auth.config;

import com.sentinel.auth.security.filters.JWTAuthenticationFilter;
import com.sentinel.auth.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

        private final JWTAuthenticationFilter jwtAuthenticationFilter;
        private final com.sentinel.auth.security.filters.RateLimitingFilter rateLimitingFilter;
        private final AuthenticationProvider authenticationProvider;
        private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

        @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:5173,http://localhost:5174,http://localhost:8000}")
        private String allowedOriginsString;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                log.info("🔧 Configuring SecurityFilterChain with OAuth2");

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/api/auth/register").permitAll()
                                                .requestMatchers("/api/auth/login").permitAll()
                                                .requestMatchers("/api/auth/refresh").permitAll()
                                                .requestMatchers("/api/auth/password/**").permitAll()
                                                .requestMatchers("/actuator/**").permitAll()

                                                // OAuth2 endpoints
                                                .requestMatchers(
                                                                "/oauth2/**",
                                                                "/login/oauth2/**",
                                                                "/api/auth/oauth2/**",
                                                                "/api/auth/login/oauth2/**")
                                                .permitAll()

                                                // Protected endpoints
                                                .requestMatchers("/api/auth/2fa/**").authenticated()
                                                .anyRequest().authenticated())

                                // Stateless (JWT)
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // OAuth2 Login
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .baseUri("/api/auth/oauth2/authorization"))
                                                .redirectionEndpoint(redirection -> redirection
                                                                .baseUri("/api/auth/oauth2/callback/*"))
                                                .successHandler(oauth2SuccessHandler)
                                                .failureUrl("http://localhost:5173/auth?error=oauth2_failed"))

                                .exceptionHandling(e -> e
                                                .authenticationEntryPoint(
                                                                new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                                                                org.springframework.http.HttpStatus.UNAUTHORIZED)))

                                .authenticationProvider(authenticationProvider)

                                // Security Headers
                                .headers(headers -> headers
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)) // 1 year
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'; " +
                                                                                "script-src 'self' 'unsafe-inline'; " +
                                                                                "style-src 'self' 'unsafe-inline'; " +
                                                                                "img-src 'self' data: https:; " +
                                                                                "font-src 'self'; " +
                                                                                "connect-src 'self'; " +
                                                                                "frame-ancestors 'none'"))
                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .xssProtection(xss -> xss.disable())
                                                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable()))

                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                // Rate limiting for auth-sensitive endpoints
                                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

                log.info("✅ SecurityFilterChain configured successfully");

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Split and trim the allowed origins
                String[] origins = Arrays.stream(allowedOriginsString.split(","))
                                .map(String::trim)
                                .toArray(String[]::new);

                log.info("🌐 CORS Allowed Origins: {}", Arrays.toString(origins));

                config.setAllowedOrigins(Arrays.asList(origins));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(Arrays.asList("*"));
                config.setAllowCredentials(true);
                // Do not expose Authorization header; tokens are delivered via secure cookies
                config.setExposedHeaders(Arrays.asList("Content-Type", "Authorization"));
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}