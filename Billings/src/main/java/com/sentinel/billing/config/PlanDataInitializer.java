package com.sentinel.billing.config;

import com.sentinel.billing.model.PlanEntity;
import com.sentinel.billing.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PlanDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(PlanDataInitializer.class);

    private final PlanRepository planRepository;

    public PlanDataInitializer(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @PostConstruct
    public void initPlans() {
        if (planRepository.count() > 0) {
            log.info("Plans ya existen en la base de datos. No se insertan datos de ejemplo.");
            return;
        }

        log.info("Insertando planes iniciales en la base de datos (FREE, PROFESSIONAL, ENTERPRISE)...");

        List<PlanEntity> plans = List.of(
                // FREE Plan - No puede crear tenants ni proyectos
                new PlanEntity()
                        .setId("FREE")
                        .setName("Free")
                        .setDescription("Plan gratuito para explorar la plataforma. Sin tenants ni proyectos.")
                        .setMonthlyPriceUsd(BigDecimal.ZERO)
                        .setMonthlyPriceCop(BigDecimal.ZERO)
                        .setMaxUsers(0)
                        .setMaxProjects(0)
                        .setMaxDomains(0)
                        .setMaxRepos(0)
                        .setMaxTenants(0)
                        .setIncludesBlockchain(false)
                        .setRecommended(false),

                // PROFESSIONAL Plan - 3 tenants, 6 proyectos, 10 usuarios por tenant
                new PlanEntity()
                        .setId("PROFESSIONAL")
                        .setName("Professional")
                        .setDescription("Ideal para equipos pequeños y medianos. Incluye 3 tenants y 6 proyectos.")
                        .setMonthlyPriceUsd(BigDecimal.valueOf(29.99))
                        .setMonthlyPriceCop(BigDecimal.valueOf(120000))
                        .setMaxUsers(10)
                        .setMaxProjects(6)
                        .setMaxDomains(3)
                        .setMaxRepos(10)
                        .setMaxTenants(3)
                        .setIncludesBlockchain(true)
                        .setRecommended(true),

                // ENTERPRISE Plan - 6 tenants, 12 proyectos, 25 usuarios por tenant
                new PlanEntity()
                        .setId("ENTERPRISE")
                        .setName("Enterprise")
                        .setDescription("Solución empresarial completa con soporte prioritario y límites extendidos.")
                        .setMonthlyPriceUsd(BigDecimal.valueOf(99.99))
                        .setMonthlyPriceCop(BigDecimal.valueOf(400000))
                        .setMaxUsers(25)
                        .setMaxProjects(12)
                        .setMaxDomains(10)
                        .setMaxRepos(30)
                        .setMaxTenants(6)
                        .setIncludesBlockchain(true)
                        .setRecommended(false));

        planRepository.saveAll(plans);
        log.info("Planes iniciales insertados correctamente: FREE, PROFESSIONAL, ENTERPRISE");
    }
}
