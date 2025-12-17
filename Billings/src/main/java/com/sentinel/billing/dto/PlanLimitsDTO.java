package com.sentinel.billing.dto;

import com.sentinel.billing.model.PlanEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para exponer límites de planes a otros microservicios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanLimitsDTO {

    private String planId;
    private String planName;
    private Integer maxUsers;
    private Integer maxProjects;
    private Integer maxDomains;
    private Integer maxRepos;
    private Boolean includesBlockchain;

    /**
     * Crea un PlanLimitsDTO desde un PlanEntity.
     */
    public static PlanLimitsDTO fromEntity(PlanEntity entity) {
        return PlanLimitsDTO.builder()
                .planId(entity.getId())
                .planName(entity.getName())
                .maxUsers(entity.getMaxUsers())
                .maxProjects(entity.getMaxProjects())
                .maxDomains(entity.getMaxDomains())
                .maxRepos(entity.getMaxRepos())
                .includesBlockchain(entity.isIncludesBlockchain())
                .build();
    }

    /**
     * Verifica si permite recursos ilimitados en proyectos.
     */
    public boolean hasUnlimitedProjects() {
        return maxProjects != null && maxProjects == -1;
    }

    /**
     * Verifica si puede crear más proyectos.
     */
    public boolean canCreateProject(int currentProjects) {
        return hasUnlimitedProjects() || currentProjects < maxProjects;
    }

    /**
     * Verifica si puede agregar más usuarios.
     */
    public boolean canAddUser(int currentUsers) {
        return currentUsers < maxUsers;
    }

    /**
     * Verifica si puede agregar más dominios.
     */
    public boolean canAddDomain(int currentDomains) {
        return currentDomains < maxDomains;
    }

    /**
     * Verifica si puede agregar más repositorios.
     */
    public boolean canAddRepo(int currentRepos) {
        return currentRepos < maxRepos;
    }
}
