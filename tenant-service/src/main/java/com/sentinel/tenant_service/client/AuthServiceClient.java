package com.sentinel.tenant_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${services.auth.url}")
public interface AuthServiceClient {

    @CircuitBreaker(name = "authService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "authService")
    @GetMapping("/api/users/{id}")
    UserResponseDTO getUserById(@PathVariable("id") UUID id);

    default UserResponseDTO getUserByIdFallback(UUID id, Exception ex) {
        return new UserResponseDTO(id, null); // Return null email if service fails
    }

    // Inner DTO matching Auth Service response
    class UserResponseDTO {
        private UUID id;
        private String email;

        public UserResponseDTO() {
        }

        public UserResponseDTO(UUID id, String email) {
            this.id = id;
            this.email = email;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
