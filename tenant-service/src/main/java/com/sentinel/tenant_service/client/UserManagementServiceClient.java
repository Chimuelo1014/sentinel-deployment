package com.sentinel.tenant_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Feign Client para comunicación con user-management-service.
 * Valida roles y permisos de usuarios.
 */
@FeignClient(name = "user-management-service", url = "${services.user_mgmt.url}")
public interface UserManagementServiceClient {

    /**
     * Obtiene el rol de un usuario en un tenant.
     * GET /api/internal/permissions/tenant/{tenantId}/user/{userId}/role
     * 
     * @return "TENANT_ADMIN" | "TENANT_USER" | null (si no es miembro)
     */
    @CircuitBreaker(name = "userMgmtService", fallbackMethod = "getTenantRoleFallback")
    @Retry(name = "userMgmtService")
    @GetMapping("/api/internal/permissions/tenant/{tenantId}/user/{userId}/role")
    String getTenantRole(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId);

    /**
     * Obtiene lista de tenants donde el usuario es miembro
     * GET /api/internal/users/{userId}/tenants
     * 
     * @return Lista de tenant IDs
     */
    @CircuitBreaker(name = "userMgmtService", fallbackMethod = "getUserTenantsFallback")
    @Retry(name = "userMgmtService")
    @GetMapping("/api/internal/users/{userId}/tenants")
    List<UUID> getUserTenants(@PathVariable UUID userId);

    /**
     * ✅ NUEVO: Obtiene lista de proyectos donde el usuario participa
     * GET /api/internal/users/{userId}/projects
     * 
     * @return Lista de project IDs
     */
    @CircuitBreaker(name = "userMgmtService", fallbackMethod = "getUserProjectsFallback")
    @Retry(name = "userMgmtService")
    @GetMapping("/api/internal/users/{userId}/projects")
    List<UUID> getUserProjects(@PathVariable UUID userId);

    /**
     * ✅ NUEVO: Obtiene el plan del usuario
     * GET /api/internal/users/{userId}/plan
     * 
     * @return UserPlanDTO con información del plan
     */
    @CircuitBreaker(name = "userMgmtService", fallbackMethod = "getUserPlanFallback")
    @Retry(name = "userMgmtService")
    @GetMapping("/api/internal/users/{userId}/plan")
    UserPlanResponse getUserPlan(@PathVariable UUID userId);

    /**
     * ✅ NUEVO: Agrega un usuario a un proyecto
     * POST /api/internal/projects/{projectId}/members
     * 
     * @param projectId ID del proyecto
     * @param userId    ID del usuario
     * @param tenantId  ID del tenant
     * @param role      Rol del usuario en el proyecto (PROJECT_ADMIN,
     *                  PROJECT_MEMBER, PROJECT_VIEWER)
     */
    @CircuitBreaker(name = "userMgmtService", fallbackMethod = "addProjectMemberFallback")
    @Retry(name = "userMgmtService")
    @PostMapping("/api/internal/projects/{projectId}/members")
    void addProjectMember(
            @PathVariable UUID projectId,
            @RequestParam UUID userId,
            @RequestParam UUID tenantId,
            @RequestParam String role);

    /**
     * Verifica si un usuario es miembro de un tenant.
     */
    default boolean isTenantMember(UUID tenantId, UUID userId) {
        try {
            String role = getTenantRole(tenantId, userId);
            return role != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fallback cuando user-management-service no responde.
     * Retorna null para indicar que no se pudo verificar.
     */
    default String getTenantRoleFallback(UUID tenantId, UUID userId, Exception ex) {
        return null;
    }

    /**
     * Fallback para getUserTenants
     */
    default List<UUID> getUserTenantsFallback(UUID userId, Exception ex) {
        return List.of();
    }

    /**
     * ✅ NUEVO: Fallback para getUserProjects
     */
    default List<UUID> getUserProjectsFallback(UUID userId, Exception ex) {
        return List.of();
    }

    /**
     * ✅ NUEVO: Fallback para getUserPlan - retorna plan FREE por defecto
     */
    default UserPlanResponse getUserPlanFallback(UUID userId, Exception ex) {
        return new UserPlanResponse("FREE");
    }

    /**
     * Fallback for addProjectMember - logs error but doesn't throw
     */
    default void addProjectMemberFallback(UUID projectId, UUID userId, UUID tenantId, String role, Exception ex) {
        // Silently fail - project member creation is not critical for invitation
        // acceptance
    }

    /**
     * DTO simple para la respuesta del plan del usuario
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    class UserPlanResponse {
        private String plan;
        private int maxTenants;
        private int maxProjectsPerTenant;
        private int maxUsersPerTenant;
        private int maxScansPerMonth;

        public UserPlanResponse() {
        }

        public UserPlanResponse(String plan) {
            this.plan = plan;
        }

        public String getPlan() {
            return plan;
        }

        public void setPlan(String plan) {
            this.plan = plan;
        }

        public int getMaxTenants() {
            return maxTenants;
        }

        public void setMaxTenants(int maxTenants) {
            this.maxTenants = maxTenants;
        }

        public int getMaxProjectsPerTenant() {
            return maxProjectsPerTenant;
        }

        public void setMaxProjectsPerTenant(int maxProjectsPerTenant) {
            this.maxProjectsPerTenant = maxProjectsPerTenant;
        }

        public int getMaxUsersPerTenant() {
            return maxUsersPerTenant;
        }

        public void setMaxUsersPerTenant(int maxUsersPerTenant) {
            this.maxUsersPerTenant = maxUsersPerTenant;
        }

        public int getMaxScansPerMonth() {
            return maxScansPerMonth;
        }

        public void setMaxScansPerMonth(int maxScansPerMonth) {
            this.maxScansPerMonth = maxScansPerMonth;
        }
    }
}
