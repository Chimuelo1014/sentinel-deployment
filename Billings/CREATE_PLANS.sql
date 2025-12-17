-- ============================================
-- SQL Para Crear Planes de Prueba en Billing-Service
-- ============================================
-- Ejecutar en la base de datos de billing-service

INSERT INTO plans (id, name, description, monthly_price_usd, monthly_price_cop, 
                   max_users, max_projects, max_domains, max_repos, 
                   includes_blockchain, recommended, created_at, updated_at)
VALUES
-- Plan BASIC
('BASIC', 'Basic Plan', 'Plan básico para equipos pequeños que están empezando', 
 9.99, 40000, 
 3, 3, 3, 1,  -- 3 usuarios, 3 proyectos, 3 dominios, 1 repo
 false, false, 
 NOW(), NOW()),

-- Plan PRO (Recomendado)
('PRO', 'Professional Plan', 'Plan profesional con más recursos para equipos en crecimiento', 
 29.99, 120000, 
 10, 10, 10, 5,  -- 10 usuarios, 10 proyectos, 10 dominios, 5 repos
 false, true,  -- recommended=true
 NOW(), NOW()),

-- Plan ENTERPRISE
('ENTERPRISE', 'Enterprise Plan', 'Plan empresarial con recursos ilimitados para grandes equipos', 
 99.99, 400000, 
 50, -1, 50, 20,  -- 50 usuarios, ILIMITADOS proyectos, 50 dominios, 20 repos
 true, false,  -- includes_blockchain=true
 NOW(), NOW());

-- Verificar que se crearon correctamente
SELECT * FROM plans ORDER BY monthly_price_usd;
