-- ============================================
-- Script de Migración: Separación Completa de Datos por Empresa
-- ============================================
-- Agrega usuario_comercial_id a todas las entidades operativas

-- ============================================
-- 1. PRODUCTOS
-- ============================================
ALTER TABLE siga_saas.PRODUCTOS 
ADD COLUMN IF NOT EXISTS usuario_comercial_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_productos_comercial_id ON siga_saas.PRODUCTOS(usuario_comercial_id);

COMMENT ON COLUMN siga_saas.PRODUCTOS.usuario_comercial_id IS 'ID del usuario comercial (dueño) al que pertenece este producto';

-- ============================================
-- 2. LOCALES
-- ============================================
ALTER TABLE siga_saas.LOCALES 
ADD COLUMN IF NOT EXISTS usuario_comercial_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_locales_comercial_id ON siga_saas.LOCALES(usuario_comercial_id);

COMMENT ON COLUMN siga_saas.LOCALES.usuario_comercial_id IS 'ID del usuario comercial (dueño) al que pertenece este local';

-- ============================================
-- 3. CATEGORIAS
-- ============================================
ALTER TABLE siga_saas.CATEGORIAS 
ADD COLUMN IF NOT EXISTS usuario_comercial_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_categorias_comercial_id ON siga_saas.CATEGORIAS(usuario_comercial_id);

COMMENT ON COLUMN siga_saas.CATEGORIAS.usuario_comercial_id IS 'ID del usuario comercial (dueño) al que pertenece esta categoría';

-- ============================================
-- 4. STOCK (ya tiene relación indirecta vía productos y locales)
-- ============================================
-- El stock se filtra automáticamente por producto_id y local_id que ya tienen usuario_comercial_id
-- No es necesario agregar campo adicional

-- ============================================
-- 5. VENTAS
-- ============================================
ALTER TABLE siga_saas.VENTAS 
ADD COLUMN IF NOT EXISTS usuario_comercial_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_ventas_comercial_id ON siga_saas.VENTAS(usuario_comercial_id);

COMMENT ON COLUMN siga_saas.VENTAS.usuario_comercial_id IS 'ID del usuario comercial (dueño) al que pertenece esta venta';

-- ============================================
-- 6. ACTUALIZAR DATOS EXISTENTES
-- ============================================
-- Relacionar productos existentes con usuarios comerciales por email del usuario que los creó
-- (si hay un usuario operativo con el mismo email que un usuario comercial)
UPDATE siga_saas.PRODUCTOS p
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc
    INNER JOIN siga_saas.USUARIOS u ON u.email = uc.email
    WHERE u.id IN (
        SELECT DISTINCT usuario_id 
        FROM siga_saas.VENTAS 
        WHERE producto_id = p.id
        LIMIT 1
    )
    LIMIT 1
)
WHERE p.usuario_comercial_id IS NULL;

-- Relacionar locales existentes (similar)
UPDATE siga_saas.LOCALES l
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc
    INNER JOIN siga_saas.USUARIOS u ON u.email = uc.email
    WHERE u.id IN (
        SELECT DISTINCT usuario_id 
        FROM siga_saas.VENTAS 
        WHERE local_id = l.id
        LIMIT 1
    )
    LIMIT 1
)
WHERE l.usuario_comercial_id IS NULL;

-- Relacionar categorías existentes (asignar a primer usuario comercial encontrado)
UPDATE siga_saas.CATEGORIAS c
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc
    INNER JOIN siga_saas.USUARIOS u ON u.email = uc.email
    LIMIT 1
)
WHERE c.usuario_comercial_id IS NULL;

-- Relacionar ventas existentes
UPDATE siga_saas.VENTAS v
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc
    INNER JOIN siga_saas.USUARIOS u ON u.email = uc.email AND u.id = v.usuario_id
    LIMIT 1
)
WHERE v.usuario_comercial_id IS NULL AND v.usuario_id IS NOT NULL;

COMMENT ON TABLE siga_saas.PRODUCTOS IS 'Productos del sistema. Separados por empresa mediante usuario_comercial_id.';
COMMENT ON TABLE siga_saas.LOCALES IS 'Locales/bodegas. Separados por empresa mediante usuario_comercial_id.';
COMMENT ON TABLE siga_saas.CATEGORIAS IS 'Categorías de productos. Separadas por empresa mediante usuario_comercial_id.';
COMMENT ON TABLE siga_saas.VENTAS IS 'Ventas del sistema. Separadas por empresa mediante usuario_comercial_id.';
