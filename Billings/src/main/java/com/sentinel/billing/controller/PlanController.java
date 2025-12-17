package com.sentinel.billing.controller;

import com.sentinel.billing.model.PlanEntity;
import com.sentinel.billing.repository.PlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for plan catalog management.
 */
@RestController
@RequestMapping("/api/plans")
// CORS handled globally by SecurityConfig
public class PlanController {

    private final PlanRepository planRepository;

    public PlanController(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Returns the plan catalog from the database.
     * GET /api/plans
     */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        List<PlanEntity> entities = planRepository.findAll();

        List<PlanResponse> response = entities.stream()
                .map(PlanResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    public static class PlanResponse {
        private String id;
        private String name;
        private String description;
        private double priceUsd;
        private double priceCop;
        private double price; // Alias for priceUsd (frontend compatibility)
        private String period = "month"; // Default period
        private int maxUsers;
        private int maxProjects;
        private int maxDomains;
        private int maxRepos;
        private int maxTenants;
        private boolean includesBlockchain;
        private boolean recommended;
        private String[] features; // Array of feature descriptions

        public static PlanResponse fromEntity(PlanEntity entity) {
            PlanResponse dto = new PlanResponse();
            dto.id = entity.getId();
            dto.name = entity.getName();
            dto.description = entity.getDescription();
            dto.priceUsd = entity.getMonthlyPriceUsd().doubleValue();
            dto.priceCop = entity.getMonthlyPriceCop().doubleValue();
            dto.price = dto.priceUsd; // Set price as alias for priceUsd
            dto.period = "month";
            dto.maxUsers = entity.getMaxUsers();
            dto.maxProjects = entity.getMaxProjects();
            dto.maxDomains = entity.getMaxDomains();
            dto.maxRepos = entity.getMaxRepos();
            dto.maxTenants = entity.getMaxTenants();
            dto.includesBlockchain = entity.isIncludesBlockchain();
            dto.recommended = entity.isRecommended();

            // Build features array from plan limits
            dto.features = new String[] {
                    dto.maxUsers + " user" + (dto.maxUsers != 1 ? "s" : ""),
                    dto.maxProjects + " project" + (dto.maxProjects != 1 ? "s" : ""),
                    dto.maxTenants + " workspace" + (dto.maxTenants != 1 ? "s" : ""),
                    dto.maxDomains + " domain" + (dto.maxDomains != 1 ? "s" : ""),
                    dto.maxRepos + " repositor" + (dto.maxRepos != 1 ? "ies" : "y")
            };

            return dto;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getPriceUsd() {
            return priceUsd;
        }

        public void setPriceUsd(double priceUsd) {
            this.priceUsd = priceUsd;
        }

        public double getPriceCop() {
            return priceCop;
        }

        public void setPriceCop(double priceCop) {
            this.priceCop = priceCop;
        }

        public int getMaxUsers() {
            return maxUsers;
        }

        public void setMaxUsers(int maxUsers) {
            this.maxUsers = maxUsers;
        }

        public int getMaxProjects() {
            return maxProjects;
        }

        public void setMaxProjects(int maxProjects) {
            this.maxProjects = maxProjects;
        }

        public int getMaxDomains() {
            return maxDomains;
        }

        public void setMaxDomains(int maxDomains) {
            this.maxDomains = maxDomains;
        }

        public int getMaxRepos() {
            return maxRepos;
        }

        public void setMaxRepos(int maxRepos) {
            this.maxRepos = maxRepos;
        }

        public int getMaxTenants() {
            return maxTenants;
        }

        public void setMaxTenants(int maxTenants) {
            this.maxTenants = maxTenants;
        }

        public boolean isIncludesBlockchain() {
            return includesBlockchain;
        }

        public void setIncludesBlockchain(boolean includesBlockchain) {
            this.includesBlockchain = includesBlockchain;
        }

        public boolean isRecommended() {
            return recommended;
        }

        public void setRecommended(boolean recommended) {
            this.recommended = recommended;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String[] getFeatures() {
            return features;
        }

        public void setFeatures(String[] features) {
            this.features = features;
        }
    }
}
