-- ============================================
-- Script de Migración: Asignar Empresas a Datos Existentes
-- ============================================
-- Asigna usuario_comercial_id a productos, locales y categorías existentes
-- basándose en el usuario operativo que los creó o el primer usuario comercial encontrado

-- ============================================
-- 1. ASIGNAR PRODUCTOS A EMPRESAS
-- ============================================
-- Asignar productos a la empresa del usuario operativo que los creó
-- Si no hay relación, asignar al primer usuario comercial encontrado
UPDATE siga_saas.PRODUCTOS p
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc
    INNER JOIN siga_saas.USUARIOS u ON u.usuario_comercial_id = uc.id
    WHERE u.id IN (
        SELECT DISTINCT usuario_id 
        FROM siga_saas.VENTAS v
        INNER JOIN siga_saas.DETALLES_VENTA dv ON dv.venta_id = v.id
        WHERE dv.producto_id = p.id
        LIMIT 1
    )
    LIMIT 1
)
WHERE p.usuario_comercial_id IS NULL;

-- Si aún hay productos sin empresa, asignar al primer usuario comercial
UPDATE siga_saas.PRODUCTOS p
SET usuario_comercial_id = (
    SELECT id FROM siga_comercial.USUARIOS ORDER BY id LIMIT 1
)
WHERE p.usuario_comercial_id IS NULL;

-- ============================================
-- 2. ASIGNAR LOCALES A EMPRESAS
-- ============================================
-- Asignar locales a la empresa del usuario operativo que los creó
UPDATE siga_saas.LOCALES l
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc
    INNER JOIN siga_saas.USUARIOS u ON u.usuario_comercial_id = uc.id
    WHERE u.id IN (
        SELECT DISTINCT usuario_id 
        FROM siga_saas.VENTAS 
        WHERE local_id = l.id
        LIMIT 1
    )
    LIMIT 1
)
WHERE l.usuario_comercial_id IS NULL;

-- Si aún hay locales sin empresa, asignar al primer usuario comercial
UPDATE siga_saas.LOCALES l
SET usuario_comercial_id = (
    SELECT id FROM siga_comercial.USUARIOS ORDER BY id LIMIT 1
)
WHERE l.usuario_comercial_id IS NULL;

-- ============================================
-- 3. ASIGNAR CATEGORÍAS A EMPRESAS
-- ============================================
-- Asignar categorías al primer usuario comercial (o al que tenga productos en esa categoría)
UPDATE siga_saas.CATEGORIAS c
SET usuario_comercial_id = (
    SELECT DISTINCT p.usuario_comercial_id
    FROM siga_saas.PRODUCTOS p
    WHERE p.categoria_id = c.id AND p.usuario_comercial_id IS NOT NULL
    LIMIT 1
)
WHERE c.usuario_comercial_id IS NULL;

-- Si aún hay categorías sin empresa, asignar al primer usuario comercial
UPDATE siga_saas.CATEGORIAS c
SET usuario_comercial_id = (
    SELECT id FROM siga_comercial.USUARIOS ORDER BY id LIMIT 1
)
WHERE c.usuario_comercial_id IS NULL;

-- ============================================
-- 4. ASIGNAR VENTAS A EMPRESAS
-- ============================================
-- Asignar ventas a la empresa del usuario que las creó
UPDATE siga_saas.VENTAS v
SET usuario_comercial_id = (
    SELECT u.usuario_comercial_id
    FROM siga_saas.USUARIOS u
    WHERE u.id = v.usuario_id AND u.usuario_comercial_id IS NOT NULL
    LIMIT 1
)
WHERE v.usuario_comercial_id IS NULL AND v.usuario_id IS NOT NULL;

-- Si aún hay ventas sin empresa, asignar al primer usuario comercial
UPDATE siga_saas.VENTAS v
SET usuario_comercial_id = (
    SELECT id FROM siga_comercial.USUARIOS ORDER BY id LIMIT 1
)
WHERE v.usuario_comercial_id IS NULL;

COMMENT ON TABLE siga_saas.PRODUCTOS IS 'Productos del sistema. Separados por empresa mediante usuario_comercial_id. Todos los productos deben tener usuario_comercial_id asignado.';
COMMENT ON TABLE siga_saas.LOCALES IS 'Locales/bodegas. Separados por empresa mediante usuario_comercial_id. Todos los locales deben tener usuario_comercial_id asignado.';
COMMENT ON TABLE siga_saas.CATEGORIAS IS 'Categorías de productos. Separadas por empresa mediante usuario_comercial_id. Todas las categorías deben tener usuario_comercial_id asignado.';
COMMENT ON TABLE siga_saas.VENTAS IS 'Ventas del sistema. Separadas por empresa mediante usuario_comercial_id. Todas las ventas deben tener usuario_comercial_id asignado.';
