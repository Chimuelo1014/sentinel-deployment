-- Limpiar planes existentes
DELETE FROM plan WHERE id IN ('1', '2', '3', '4', 'FREE', 'PROFESSIONAL', 'ENTERPRISE', 'BASIC', 'STANDARD', 'PRO');

-- Insertar los 3 planes: FREE, PRO, ENTERPRISE
INSERT INTO plan (
  id,
  name,
  description,
  monthly_price_usd,
  monthly_price_cop,
  max_users,
  max_projects,
  max_domains,
  max_repos,
  max_tenants,
  includes_blockchain,
  recommended,
  created_at,
  updated_at
) VALUES
(
  'FREE',
  'Free',
  'Plan gratuito para explorar la plataforma. 1 usuario, 1 proyecto.',
  0.00,
  0,
  1,
  1,
  1,
  1,
  1,
  false,
  false,
  NOW(),
  NOW()
),
(
  'PRO',
  'Pro',
  'Ideal para equipos pequeños y medianos. Incluye 3 tenants y 6 proyectos.',
  29.99,
  120000,
  10,
  6,
  3,
  10,
  3,
  true,
  true,
  NOW(),
  NOW()
),
(
  'ENTERPRISE',
  'Enterprise',
  'Solución empresarial completa con soporte prioritario y límites extendidos.',
  99.99,
  400000,
  25,
  12,
  10,
  30,
  6,
  true,
  false,
  NOW(),
  NOW()
);
