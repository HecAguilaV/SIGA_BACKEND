-- ============================================
-- Migración: Corregir esquema de FACTURAS (siga_comercial)
-- ============================================
-- Problema:
-- - Existía una tabla FACTURAS con estructura antigua (monto/iva/total/fecha_emision),
--   mientras que el backend actual usa campos denormalizados (usuario_nombre, plan_nombre, precio_uf, etc.).
--
-- Solución:
-- - Si la tabla existe y está vacía, se recrea con el esquema correcto.
-- - Si la tabla tiene datos, NO se toca (para evitar pérdida).
--
-- IMPORTANTE:
-- - Ejecutar en la BD destino (Railway/AlwaysData) con permisos de DDL.

DO $$
DECLARE
  tiene_datos BOOLEAN := FALSE;
  existe_tabla BOOLEAN := FALSE;
BEGIN
  SELECT EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'siga_comercial'
      AND table_name = 'facturas'
  ) INTO existe_tabla;

  IF existe_tabla THEN
    EXECUTE 'SELECT EXISTS (SELECT 1 FROM siga_comercial.facturas LIMIT 1)';
    -- El EXECUTE anterior no puede asignar directo sin INTO en dynamic SQL:
    EXECUTE 'SELECT EXISTS (SELECT 1 FROM siga_comercial.facturas LIMIT 1)' INTO tiene_datos;
  END IF;

  IF existe_tabla AND tiene_datos THEN
    RAISE NOTICE 'siga_comercial.FACTURAS tiene datos; no se recrea automáticamente.';
    RETURN;
  END IF;

  -- Si existe pero está vacía, la recreamos para alinear con el backend actual
  IF existe_tabla THEN
    EXECUTE 'DROP TABLE siga_comercial.FACTURAS';
  END IF;

  EXECUTE $SQL$
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
  $SQL$;

  RAISE NOTICE 'siga_comercial.FACTURAS recreada con el esquema correcto (backend actual).';
END $$;

