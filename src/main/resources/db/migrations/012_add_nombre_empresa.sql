-- ============================================
-- Agregar campo nombre_empresa a usuarios comerciales
-- ============================================

ALTER TABLE siga_comercial.USUARIOS 
ADD COLUMN IF NOT EXISTS nombre_empresa VARCHAR(255);

COMMENT ON COLUMN siga_comercial.USUARIOS.nombre_empresa IS 'Nombre de la empresa del cliente (opcional)';
