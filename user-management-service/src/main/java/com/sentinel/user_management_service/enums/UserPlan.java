package com.sentinel.user_management_service.enums;

public enum UserPlan {
    // FREE: 0 tenants, 0 projects, 0 users
    FREE(0, 0, 0, 0),

    // PROFESSIONAL: 3 tenants, 6 projects total, 10 users/tenant
    PROFESSIONAL(3, 6, 10, 500),

    // ENTERPRISE: 6 tenants, 12 projects total, 25 users/tenant
    ENTERPRISE(6, 12, 25, 999999),

    // Legacy plans for backwards compatibility
    STANDARD(1, 3, 5, 100),
    PRO(3, 6, 10, 500),
    BASIC(1, 3, 5, 100);

    private final int maxTenants;
    private final int maxProjectsPerTenant;
    private final int maxUsersPerTenant;
    private final int maxScansPerMonth;

    UserPlan(int maxTenants, int maxProjectsPerTenant, int maxUsersPerTenant, int maxScansPerMonth) {
        this.maxTenants = maxTenants;
        this.maxProjectsPerTenant = maxProjectsPerTenant;
        this.maxUsersPerTenant = maxUsersPerTenant;
        this.maxScansPerMonth = maxScansPerMonth;
    }

    public int getMaxTenants() {
        return maxTenants;
    }

    public int getMaxProjectsPerTenant() {
        return maxProjectsPerTenant;
    }

    public int getMaxUsersPerTenant() {
        return maxUsersPerTenant;
    }

    public int getMaxScansPerMonth() {
        return maxScansPerMonth;
    }
}