package com.sentinel.tenant_service.exception;

/**
 * Exception lanzada cuando un usuario con plan FREE intenta crear un tenant.
 * Los usuarios FREE solo pueden aceptar invitaciones, no crear workspaces
 * propios.
 */
public class PlanUpgradeRequiredException extends RuntimeException {

    public PlanUpgradeRequiredException(String message) {
        super(message);
    }

    public PlanUpgradeRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
