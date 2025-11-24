-- ============================================
-- Script de Migración: Esquema siga_comercial
-- ============================================
-- Tablas del portal comercial y gestión de suscripciones

-- ============================================
-- TABLA: USUARIOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.USUARIOS (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    rut VARCHAR(20),
    telefono VARCHAR(20),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.USUARIOS IS 'Clientes del portal comercial';

-- ============================================
-- TABLA: PLANES
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.PLANES (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio_mensual DECIMAL(10,2) NOT NULL CHECK (precio_mensual >= 0),
    precio_anual DECIMAL(10,2) CHECK (precio_anual >= 0),
    limite_bodegas INTEGER DEFAULT 1 CHECK (limite_bodegas > 0),
    limite_usuarios INTEGER DEFAULT 1 CHECK (limite_usuarios > 0),
    limite_productos INTEGER,
    caracteristicas JSONB, -- Características del plan en formato JSON
    activo BOOLEAN DEFAULT true,
    orden INTEGER DEFAULT 0, -- Para ordenar en la página de planes
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.PLANES IS 'Planes de suscripción disponibles';
COMMENT ON COLUMN siga_comercial.PLANES.caracteristicas IS 'JSON con características del plan: {"trial_gratis": true, "soporte": "email", etc}';

-- ============================================
-- TABLA: SUSCRIPCIONES
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.SUSCRIPCIONES (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE,
    plan_id INTEGER REFERENCES siga_comercial.PLANES(id),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'SUSPENDIDA', 'CANCELADA', 'VENCIDA')),
    periodo VARCHAR(20) NOT NULL DEFAULT 'MENSUAL' CHECK (periodo IN ('MENSUAL', 'ANUAL')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.SUSCRIPCIONES IS 'Suscripciones activas de clientes';
CREATE INDEX idx_suscripciones_usuario ON siga_comercial.SUSCRIPCIONES(usuario_id);
CREATE INDEX idx_suscripciones_plan ON siga_comercial.SUSCRIPCIONES(plan_id);
CREATE INDEX idx_suscripciones_estado ON siga_comercial.SUSCRIPCIONES(estado);

-- ============================================
-- TABLA: PAGOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.PAGOS (
    id SERIAL PRIMARY KEY,
    suscripcion_id INTEGER REFERENCES siga_comercial.SUSCRIPCIONES(id) ON DELETE CASCADE,
    monto DECIMAL(10,2) NOT NULL CHECK (monto >= 0),
    moneda VARCHAR(10) DEFAULT 'CLP',
    metodo_pago VARCHAR(50), -- 'TARJETA', 'TRANSFERENCIA', 'OTRO'
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'COMPLETADO', 'FALLIDO', 'REEMBOLSADO')),
    referencia_externa VARCHAR(255), -- ID de transacción del procesador de pagos
    fecha_pago TIMESTAMP,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.PAGOS IS 'Registro de pagos de suscripciones';
CREATE INDEX idx_pagos_suscripcion ON siga_comercial.PAGOS(suscripcion_id);
CREATE INDEX idx_pagos_estado ON siga_comercial.PAGOS(estado);
CREATE INDEX idx_pagos_fecha ON siga_comercial.PAGOS(fecha_pago);

-- ============================================
-- TABLA: FACTURAS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.FACTURAS (
    id SERIAL PRIMARY KEY,
    suscripcion_id INTEGER REFERENCES siga_comercial.SUSCRIPCIONES(id) ON DELETE CASCADE,
    pago_id INTEGER REFERENCES siga_comercial.PAGOS(id),
    numero_factura VARCHAR(50) UNIQUE,
    monto DECIMAL(10,2) NOT NULL CHECK (monto >= 0),
    iva DECIMAL(10,2) DEFAULT 0 CHECK (iva >= 0),
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'ANULADA')),
    archivo_pdf TEXT, -- URL o path al PDF de la factura
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_comercial.FACTURAS IS 'Facturas generadas para suscripciones';
CREATE INDEX idx_facturas_suscripcion ON siga_comercial.FACTURAS(suscripcion_id);
CREATE INDEX idx_facturas_pago ON siga_comercial.FACTURAS(pago_id);
CREATE INDEX idx_facturas_estado ON siga_comercial.FACTURAS(estado);

-- ============================================
-- TABLA: CARRITOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_comercial.CARRITOS (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE,
    plan_id INTEGER REFERENCES siga_comercial.PLANES(id),
    periodo VARCHAR(20) NOT NULL DEFAULT 'MENSUAL' CHECK (periodo IN ('MENSUAL', 'ANUAL')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(usuario_id) -- Un carrito por usuario
);

COMMENT ON TABLE siga_comercial.CARRITOS IS 'Carritos de compra de planes';
CREATE INDEX idx_carritos_usuario ON siga_comercial.CARRITOS(usuario_id);
