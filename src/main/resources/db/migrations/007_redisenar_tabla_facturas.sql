-- ============================================
-- Script de Migración: Rediseñar Tabla Facturas
-- ============================================
-- Basado en ESPECIFICACION_BACKEND_DEFINITIVA.md v2.0
-- IMPORTANTE: Esta migración requiere backup de datos existentes si hay facturas

-- ============================================
-- 1. CREAR TABLA TEMPORAL CON NUEVA ESTRUCTURA
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.FACTURAS_NUEVA (
    id SERIAL PRIMARY KEY,
    numero_factura VARCHAR(50) UNIQUE NOT NULL,
    usuario_id INTEGER NOT NULL REFERENCES siga_comercial.USUARIOS(id),
    usuario_nombre VARCHAR(255) NOT NULL,        -- Campo denormalizado
    usuario_email VARCHAR(255) NOT NULL,         -- Campo denormalizado
    plan_id INTEGER NOT NULL REFERENCES siga_comercial.PLANES(id),
    plan_nombre VARCHAR(255) NOT NULL,            -- Campo denormalizado
    precio_uf DECIMAL(10, 2) NOT NULL,
    precio_clp DECIMAL(12, 2) NULL,
    unidad VARCHAR(10) NOT NULL DEFAULT 'UF',
    fecha_compra TIMESTAMP NOT NULL,
    fecha_vencimiento TIMESTAMP NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'pagada' CHECK (estado IN ('PENDIENTE', 'pagada', 'cancelada', 'reembolsada', 'VENCIDA')),
    metodo_pago VARCHAR(100) NULL,
    ultimos_4_digitos VARCHAR(4) NULL,
    -- Campos opcionales para mantener compatibilidad
    suscripcion_id INTEGER REFERENCES siga_comercial.SUSCRIPCIONES(id) ON DELETE SET NULL,
    pago_id INTEGER REFERENCES siga_comercial.PAGOS(id) ON DELETE SET NULL,
    iva DECIMAL(10, 2) DEFAULT 0,  -- Opcional para futuro
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.FACTURAS_NUEVA IS 'Facturas con estructura nueva según especificación';
COMMENT ON COLUMN siga_comercial.FACTURAS_NUEVA.usuario_nombre IS 'Nombre del usuario al momento de crear factura (denormalizado)';
COMMENT ON COLUMN siga_comercial.FACTURAS_NUEVA.usuario_email IS 'Email del usuario al momento de crear factura (denormalizado)';
COMMENT ON COLUMN siga_comercial.FACTURAS_NUEVA.plan_nombre IS 'Nombre del plan al momento de crear factura (denormalizado)';

-- Índices
CREATE INDEX idx_facturas_nueva_numero ON siga_comercial.FACTURAS_NUEVA(numero_factura);
CREATE INDEX idx_facturas_nueva_usuario ON siga_comercial.FACTURAS_NUEVA(usuario_id);
CREATE INDEX idx_facturas_nueva_fecha_compra ON siga_comercial.FACTURAS_NUEVA(fecha_compra);
CREATE INDEX idx_facturas_nueva_estado ON siga_comercial.FACTURAS_NUEVA(estado);

-- ============================================
-- 2. MIGRAR DATOS EXISTENTES (SI HAY)
-- ============================================
-- NOTA: Si hay facturas existentes, necesitamos migrarlas
-- Por ahora, asumimos que no hay facturas en producción

-- Ejemplo de migración (comentado, descomentar si hay datos):
/*
INSERT INTO siga_comercial.FACTURAS_NUEVA (
    numero_factura, usuario_id, usuario_nombre, usuario_email, 
    plan_id, plan_nombre, precio_uf, precio_clp, unidad,
    fecha_compra, fecha_vencimiento, estado, suscripcion_id, pago_id
)
SELECT 
    f.numero_factura,
    s.usuario_id,
    u.nombre as usuario_nombre,
    u.email as usuario_email,
    s.plan_id,
    p.nombre as plan_nombre,
    f.monto as precio_uf,  -- Ajustar según lógica de negocio
    f.monto as precio_clp, -- Ajustar según lógica de negocio
    'UF' as unidad,
    f.fecha_emision::timestamp as fecha_compra,
    f.fecha_vencimiento::timestamp as fecha_vencimiento,
    CASE 
        WHEN f.estado = 'PAGADA' THEN 'pagada'
        WHEN f.estado = 'ANULADA' THEN 'cancelada'
        WHEN f.estado = 'VENCIDA' THEN 'VENCIDA'
        ELSE 'PENDIENTE'
    END as estado,
    f.suscripcion_id,
    f.pago_id
FROM siga_comercial.FACTURAS f
LEFT JOIN siga_comercial.SUSCRIPCIONES s ON f.suscripcion_id = s.id
LEFT JOIN siga_comercial.USUARIOS u ON s.usuario_id = u.id
LEFT JOIN siga_comercial.PLANES p ON s.plan_id = p.id;
*/

-- ============================================
-- 3. ELIMINAR TABLA ANTIGUA Y RENOMBRAR
-- ============================================
-- CUIDADO: Solo ejecutar si no hay datos críticos o después de migración

-- Paso 1: Eliminar tabla antigua
DROP TABLE IF EXISTS siga_comercial.FACTURAS CASCADE;

-- Paso 2: Renombrar tabla nueva
ALTER TABLE siga_comercial.FACTURAS_NUEVA RENAME TO FACTURAS;

-- ============================================
-- NOTAS IMPORTANTES:
-- ============================================
-- 1. Campos denormalizados (usuario_nombre, usuario_email, plan_nombre) son OBLIGATORIOS
-- 2. Se copian al crear factura, NO se actualizan después (son históricos)
-- 3. Relación directa con usuario_id y plan_id (más flexible)
-- 4. suscripcion_id y pago_id son opcionales (para mantener relación si aplica)
-- 5. Formato número_factura: FAC-YYYYMMDD-XXXX (generado automáticamente)
