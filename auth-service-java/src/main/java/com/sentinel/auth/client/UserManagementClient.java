package com.sentinel.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client para comunicarse con user-management-service
 */
@FeignClient(name = "user-management-service", url = "${services.user-management.url:http://user-management-service:8085}")
public interface UserManagementClient {

    /**
     * Obtener plan del usuario desde user_plans table
     * GET /api/internal/users/{userId}/plan
     */
    @GetMapping("/api/internal/users/{userId}/plan")
    UserPlanResponse getUserPlan(@PathVariable("userId") UUID userId);

    /**
     * DTO para respuesta del plan del usuario
     */
    record UserPlanResponse(
            String id,
            String userId,
            String plan,
            Integer maxTenants,
            Integer maxProjectsPerTenant,
            Integer maxUsersPerTenant,
            Integer maxScansPerMonth) {
    }
}
