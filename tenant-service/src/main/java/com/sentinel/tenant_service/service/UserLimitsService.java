package com.sentinel.tenant_service.service;

import com.sentinel.tenant_service.client.UserManagementServiceClient;
import com.sentinel.tenant_service.dto.response.LimitValidationResponse;
import com.sentinel.tenant_service.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio para validar límites del plan del usuario.
 * Los límites están a nivel de usuario, no de tenant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLimitsService {

    private final TenantRepository tenantRepository;
    private final UserManagementServiceClient userMgmtClient;

    /**
     * Valida si el usuario puede crear un nuevo tenant.
     * Consulta su plan y cuenta sus tenants actuales.
     */
    public LimitValidationResponse validateUserTenantLimit(UUID userId) {
        try {
            // 1. Obtener plan del usuario desde user-mgmt
            var userPlan = userMgmtClient.getUserPlan(userId);

            // 2. Contar tenants del usuario (como owner)
            long currentTenantCount = tenantRepository.countByOwnerId(userId);

            // 3. Validar según plan
            int maxTenants = getMaxTenants(userPlan.getPlan());

            log.debug("User {} - Plan: {}, Tenants: {}/{}",
                    userId, userPlan.getPlan(), currentTenantCount, maxTenants);

            if (currentTenantCount < maxTenants) {
                return LimitValidationResponse.allowed(maxTenants, (int) currentTenantCount);
            }

            return LimitValidationResponse.denied(
                    maxTenants,
                    (int) currentTenantCount,
                    "Tenant limit reached",
                    "Upgrade your plan to create more workspaces");

        } catch (Exception e) {
            log.error("Error checking tenant limit for user {}: {}", userId, e.getMessage());
            // Fallback: Allow creation if service check fails (Fail Open for resilience)
            return LimitValidationResponse.allowed(1, 0);
        }
    }

    /**
     * Valida si el usuario puede crear un nuevo proyecto.
     * Cuenta TODOS sus proyectos en TODOS sus tenants.
     */
    public boolean canCreateProject(UUID userId) {
        try {
            // 1. Obtener plan del usuario
            var userPlan = userMgmtClient.getUserPlan(userId);

            // 2. Contar proyectos del usuario (consultar user-mgmt)
            int currentProjectCount = getUserProjectCount(userId);

            // 3. Validar según plan
            int maxProjects = getMaxProjects(userPlan.getPlan());

            log.debug("User {} - Plan: {}, Projects: {}/{}",
                    userId, userPlan.getPlan(), currentProjectCount, maxProjects);

            if (maxProjects == -1) {
                return true; // Unlimited
            }

            return currentProjectCount < maxProjects;

        } catch (Exception e) {
            log.error("Error checking project limit for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el plan del usuario desde user-management
     */
    public String getUserPlan(UUID userId) {
        try {
            var userPlan = userMgmtClient.getUserPlan(userId);
            return userPlan.getPlan();
        } catch (Exception e) {
            log.warn("Could not fetch user plan for {}, defaulting to FREE: {}", userId, e.getMessage());
            return "FREE";
        }
    }

    /**
     * Obtiene la cantidad total de proyectos del usuario
     */
    private int getUserProjectCount(UUID userId) {
        try {
            // Llamar al nuevo endpoint que retorna List<UUID>
            var response = userMgmtClient.getUserProjects(userId);
            return response != null ? response.size() : 0;
        } catch (Exception e) {
            log.warn("Could not fetch user projects: {}", e.getMessage());
            return 0;
        }
    }

    private int getMaxTenants(String plan) {
        return switch (plan.toUpperCase()) {
            case "FREE" -> 0; // FREE: cannot create tenants
            case "PROFESSIONAL" -> 3; // PROFESSIONAL: 3 tenants
            case "ENTERPRISE" -> 6; // ENTERPRISE: 6 tenants
            // Legacy plan support
            case "BASIC", "STANDARD" -> 1;
            case "PRO" -> 3;
            default -> 0;
        };
    }

    private int getMaxProjects(String plan) {
        return switch (plan.toUpperCase()) {
            case "FREE" -> 0; // FREE: cannot create projects
            case "PROFESSIONAL" -> 6; // PROFESSIONAL: 6 projects total
            case "ENTERPRISE" -> 12; // ENTERPRISE: 12 projects total
            // Legacy plan support
            case "BASIC", "STANDARD" -> 3;
            case "PRO" -> 6;
            default -> 0;
        };
    }
}
