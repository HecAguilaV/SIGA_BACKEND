-- ============================================
-- Script de Migración: Agregar Campos a Usuarios Comerciales
-- ============================================
-- Basado en ESPECIFICACION_BACKEND_DEFINITIVA.md v2.0

-- ============================================
-- 1. AGREGAR CAMPOS DE TRIAL
-- ============================================
ALTER TABLE siga_comercial.USUARIOS 
ADD COLUMN IF NOT EXISTS en_trial BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS fecha_inicio_trial TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS fecha_fin_trial TIMESTAMP NULL;

COMMENT ON COLUMN siga_comercial.USUARIOS.en_trial IS 'Indica si el usuario está en período de trial';
COMMENT ON COLUMN siga_comercial.USUARIOS.fecha_inicio_trial IS 'Fecha de inicio del trial (14 días)';
COMMENT ON COLUMN siga_comercial.USUARIOS.fecha_fin_trial IS 'Fecha de fin del trial';

-- ============================================
-- 2. AGREGAR CAMPO ROL
-- ============================================
ALTER TABLE siga_comercial.USUARIOS 
ADD COLUMN IF NOT EXISTS rol VARCHAR(20) DEFAULT 'cliente' CHECK (rol IN ('admin', 'cliente'));

COMMENT ON COLUMN siga_comercial.USUARIOS.rol IS 'Rol del usuario: admin o cliente';

-- ============================================
-- 3. AGREGAR CAMPO PLAN_ID (CACHE)
-- ============================================
ALTER TABLE siga_comercial.USUARIOS 
ADD COLUMN IF NOT EXISTS plan_id INTEGER REFERENCES siga_comercial.PLANES(id);

COMMENT ON COLUMN siga_comercial.USUARIOS.plan_id IS 'ID del plan actual (cache, se sincroniza con suscripción activa)';
CREATE INDEX IF NOT EXISTS idx_usuarios_plan_id ON siga_comercial.USUARIOS(plan_id);

-- ============================================
-- 4. ACTUALIZAR VALORES POR DEFECTO
-- ============================================
-- Los usuarios existentes se mantienen con rol 'cliente' por defecto
-- Los campos de trial se inicializan en NULL/FALSE

-- ============================================
-- NOTAS:
-- ============================================
-- - en_trial: Indica si está en trial activo
-- - fecha_inicio_trial / fecha_fin_trial: Controlan duración del trial (14 días)
-- - rol: Permite diferenciar admin de cliente
-- - plan_id: Cache del plan actual para performance (se sincroniza con suscripción activa)
