package com.sentinel.billing.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "plan")
public class PlanEntity {

    @Id
    @Column(name = "id", length = 32, nullable = false, updatable = false)
    private String id; // BASIC, STANDARD, PRO, ENTERPRISE

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "monthly_price_usd", precision = 10, scale = 2, nullable = false)
    private BigDecimal monthlyPriceUsd;

    @Column(name = "monthly_price_cop", precision = 19, scale = 2, nullable = false)
    private BigDecimal monthlyPriceCop;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "max_projects", nullable = false)
    private Integer maxProjects;

    @Column(name = "max_domains", nullable = false)
    private Integer maxDomains;

    @Column(name = "max_repos", nullable = false)
    private Integer maxRepos;

    @Column(name = "max_tenants", nullable = false)
    private Integer maxTenants;

    @Column(name = "includes_blockchain", nullable = false)
    private boolean includesBlockchain;

    @Column(name = "recommended", nullable = false)
    private boolean recommended;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public PlanEntity() {
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ------------ getters & setters ------------

    public String getId() {
        return id;
    }

    public PlanEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PlanEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PlanEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getMonthlyPriceUsd() {
        return monthlyPriceUsd;
    }

    public PlanEntity setMonthlyPriceUsd(BigDecimal monthlyPriceUsd) {
        this.monthlyPriceUsd = monthlyPriceUsd;
        return this;
    }

    public BigDecimal getMonthlyPriceCop() {
        return monthlyPriceCop;
    }

    public PlanEntity setMonthlyPriceCop(BigDecimal monthlyPriceCop) {
        this.monthlyPriceCop = monthlyPriceCop;
        return this;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public PlanEntity setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
        return this;
    }

    public Integer getMaxProjects() {
        return maxProjects;
    }

    public PlanEntity setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
        return this;
    }

    public Integer getMaxDomains() {
        return maxDomains;
    }

    public PlanEntity setMaxDomains(Integer maxDomains) {
        this.maxDomains = maxDomains;
        return this;
    }

    public Integer getMaxRepos() {
        return maxRepos;
    }

    public PlanEntity setMaxRepos(Integer maxRepos) {
        this.maxRepos = maxRepos;
        return this;
    }

    public Integer getMaxTenants() {
        return maxTenants;
    }

    public PlanEntity setMaxTenants(Integer maxTenants) {
        this.maxTenants = maxTenants;
        return this;
    }

    public boolean isIncludesBlockchain() {
        return includesBlockchain;
    }

    public PlanEntity setIncludesBlockchain(boolean includesBlockchain) {
        this.includesBlockchain = includesBlockchain;
        return this;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public PlanEntity setRecommended(boolean recommended) {
        this.recommended = recommended;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public PlanEntity setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PlanEntity setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
