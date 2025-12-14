-- ============================================
-- Script de Migración: Agregar usuario_comercial_id a Usuarios Operativos
-- ============================================
-- Permite separar usuarios operativos por empresa/usuario comercial

-- ============================================
-- 1. AGREGAR CAMPO usuario_comercial_id
-- ============================================
ALTER TABLE siga_saas.USUARIOS 
ADD COLUMN IF NOT EXISTS usuario_comercial_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE;

COMMENT ON COLUMN siga_saas.USUARIOS.usuario_comercial_id IS 'ID del usuario comercial (dueño) al que pertenece este usuario operativo. NULL para usuarios legacy.';

-- ============================================
-- 2. CREAR ÍNDICE PARA MEJORAR RENDIMIENTO
-- ============================================
CREATE INDEX IF NOT EXISTS idx_usuarios_comercial_id ON siga_saas.USUARIOS(usuario_comercial_id);

-- ============================================
-- 3. ACTUALIZAR USUARIOS EXISTENTES
-- ============================================
-- Para usuarios existentes, intentar relacionarlos con su usuario comercial por email
UPDATE siga_saas.USUARIOS u
SET usuario_comercial_id = (
    SELECT uc.id 
    FROM siga_comercial.USUARIOS uc 
    WHERE uc.email = u.email 
    LIMIT 1
)
WHERE u.usuario_comercial_id IS NULL;

COMMENT ON TABLE siga_saas.USUARIOS IS 'Usuarios operativos del sistema. Ahora separados por empresa mediante usuario_comercial_id.';
