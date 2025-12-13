-- ============================================
-- Migración: Corregir esquema de FACTURAS (siga_comercial)
-- ============================================
-- Este archivo está escrito en SQL PLANO (sin DO $$) para compatibilidad con phpPgAdmin/AlwaysData.
--
-- ⚠️ IMPORTANTE (manual):
-- 1) Verifica que la tabla esté vacía:
--    SELECT COUNT(*) FROM siga_comercial.facturas;
-- 2) Si el conteo es 0, ejecuta TODO este script.
-- 3) Si el conteo es > 0, NO lo ejecutes (haría DROP y perderías datos).

-- 1) Eliminar tabla antigua (solo si está vacía, ver pasos arriba)
DROP TABLE IF EXISTS siga_comercial.FACTURAS;

-- 2) Crear tabla con el esquema que usa el backend actual (campos denormalizados)
CREATE TABLE IF NOT EXISTS siga_comercial.FACTURAS (
  id SERIAL PRIMARY KEY,

  -- Relación opcional con suscripción/pago (para futuro)
  suscripcion_id INTEGER REFERENCES siga_comercial.SUSCRIPCIONES(id) ON DELETE SET NULL,
  pago_id INTEGER REFERENCES siga_comercial.PAGOS(id) ON DELETE SET NULL,

  -- Identidad
  numero_factura VARCHAR(50) UNIQUE NOT NULL,

  -- Campos denormalizados (evitar JOINs en impresión)
  usuario_id INTEGER NOT NULL REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE,
  usuario_nombre VARCHAR(255) NOT NULL,
  usuario_email VARCHAR(255) NOT NULL,
  plan_id INTEGER NOT NULL REFERENCES siga_comercial.PLANES(id),
  plan_nombre VARCHAR(255) NOT NULL,

  -- Precios
  precio_uf DECIMAL(10,2) NOT NULL CHECK (precio_uf >= 0),
  precio_clp DECIMAL(12,2) CHECK (precio_clp >= 0),
  unidad VARCHAR(10) NOT NULL DEFAULT 'UF',

  -- Fechas
  fecha_compra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_vencimiento TIMESTAMP NULL,

  -- Estado y pago
  estado VARCHAR(20) NOT NULL DEFAULT 'PAGADA' CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'ANULADA')),
  metodo_pago VARCHAR(100),
  ultimos_4_digitos VARCHAR(4),

  -- IVA (opcional, futuro)
  iva DECIMAL(10,2) CHECK (iva >= 0),

  -- Auditoría
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.FACTURAS IS 'Facturas generadas para suscripciones';

CREATE INDEX IF NOT EXISTS idx_facturas_usuario_id ON siga_comercial.FACTURAS(usuario_id);
CREATE INDEX IF NOT EXISTS idx_facturas_plan_id ON siga_comercial.FACTURAS(plan_id);
CREATE INDEX IF NOT EXISTS idx_facturas_suscripcion ON siga_comercial.FACTURAS(suscripcion_id);
CREATE INDEX IF NOT EXISTS idx_facturas_pago ON siga_comercial.FACTURAS(pago_id);
CREATE INDEX IF NOT EXISTS idx_facturas_estado ON siga_comercial.FACTURAS(estado);

