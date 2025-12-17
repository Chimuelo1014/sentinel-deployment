package com.sentinel.billing.config;

import com.sentinel.billing.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:5173,http://localhost:8000}")
        private String allowedOriginsString;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/api/plans").permitAll()
                                                .requestMatchers("/api/plans/**").permitAll()
                                                .requestMatchers("/api/payments/webhook/**").permitAll()
                                                .requestMatchers("/api/webhooks/**").permitAll() // MercadoPago IPN

                                                // Actuator
                                                .requestMatchers("/actuator/**").permitAll()

                                                // For internal service calls
                                                .requestMatchers("/api/subscriptions/**").permitAll()
                                                .requestMatchers("/api/payments/**").permitAll()

                                                // Resto requiere autenticación JWT
                                                .anyRequest().permitAll()) // Temporarily allow all for testing

                                // Security Headers
                                .headers(headers -> headers
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000))
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'; " +
                                                                                "script-src 'self'; " +
                                                                                "style-src 'self'; " +
                                                                                "frame-ancestors 'none'"))
                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable()))

                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Split and trim the allowed origins
                String[] origins = Arrays.stream(allowedOriginsString.split(","))
                                .map(String::trim)
                                .toArray(String[]::new);

                log.info("🌐 Billing-Service CORS Allowed Origins: {}", Arrays.toString(origins));

                // Use setAllowedOriginPatterns when allowCredentials is true
                config.setAllowedOriginPatterns(Arrays.asList(origins));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(Arrays.asList("*"));
                config.setExposedHeaders(Arrays.asList("Content-Type", "Authorization"));
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}
