package com.sentinel.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Planes de suscripción a nivel de USUARIO (no tenant).
 * Define límites globales para el usuario.
 */
@Getter
@RequiredArgsConstructor
public enum UserPlan {

    FREE(
            "Free Plan",
            1, // maxTenants
            1, // maxProjects (total en todos los tenants)
            1, // maxDomains (total)
            0, // maxRepos
            false, // blockchainEnabled
            false// aiEnabled
    ),

    STANDARD(
            "Standard Plan - $29/mes",
            2, // maxTenants
            5, // maxProjects
            5, // maxDomains
            3, // maxRepos
            false, // blockchainEnabled
            false// aiEnabled
    ),

    BASIC(
            "Basic Plan - $15/mes",
            1, // maxTenants
            3, // maxProjects
            3, // maxDomains
            1, // maxRepos
            false, // blockchainEnabled
            false// aiEnabled
    ),

    PRO(
            "Pro Plan - $39/mes",
            3, // maxTenants
            10, // maxProjects
            10, // maxDomains
            5, // maxRepos
            false, // blockchainEnabled
            true// aiEnabled (básica)
    ),

    ENTERPRISE(
            "Enterprise Plan - $99/mes",
            10, // maxTenants
            -1, // maxProjects (-1 = unlimited)
            50, // maxDomains
            20, // maxRepos
            true, // blockchainEnabled
            true// aiEnabled (avanzada)
    );

    private final String displayName;
    private final int maxTenants;
    private final int maxProjects;
    private final int maxDomains;
    private final int maxRepos;
    private final boolean blockchainEnabled;
    private final boolean aiEnabled;

    public boolean hasUnlimitedProjects() {
        return maxProjects == -1;
    }

    public boolean canCreateTenant(int currentTenantCount) {
        return currentTenantCount < maxTenants;
    }

    public boolean canCreateProject(int currentProjectCount) {
        return hasUnlimitedProjects() || currentProjectCount < maxProjects;
    }

    public boolean canAddDomain(int currentDomainCount) {
        return currentDomainCount < maxDomains;
    }

    public boolean canAddRepo(int currentRepoCount) {
        return currentRepoCount < maxRepos;
    }

    public static UserPlan fromString(String plan) {
        try {
            return UserPlan.valueOf(plan.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FREE;
        }
    }
}