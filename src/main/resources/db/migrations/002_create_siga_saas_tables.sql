-- ============================================
-- Script de Migración: Esquema siga_saas
-- ============================================
-- Tablas del sistema operativo de gestión de inventario y ventas

-- ============================================
-- TABLA: USUARIOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.USUARIOS (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'OPERADOR', 'CAJERO')),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.USUARIOS IS 'Usuarios operativos del sistema (ADMINISTRADOR, OPERADOR, CAJERO)';
COMMENT ON COLUMN siga_saas.USUARIOS.rol IS 'Rol del usuario: ADMINISTRADOR, OPERADOR o CAJERO';

-- ============================================
-- TABLA: LOCALES
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.LOCALES (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion TEXT,
    ciudad VARCHAR(100),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.LOCALES IS 'Bodegas/sucursales donde se almacena inventario';

-- ============================================
-- TABLA: USUARIOS_LOCALES (Relación muchos a muchos)
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.USUARIOS_LOCALES (
    usuario_id INTEGER REFERENCES siga_saas.USUARIOS(id) ON DELETE CASCADE,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, local_id)
);

COMMENT ON TABLE siga_saas.USUARIOS_LOCALES IS 'Relación entre usuarios y locales asignados (OPERADOR solo ve sus locales)';

-- ============================================
-- TABLA: CATEGORIAS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.CATEGORIAS (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activa BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.CATEGORIAS IS 'Categorías de productos';

-- ============================================
-- TABLA: PRODUCTOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.PRODUCTOS (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria_id INTEGER REFERENCES siga_saas.CATEGORIAS(id),
    codigo_barras VARCHAR(50) UNIQUE,
    precio_unitario DECIMAL(10,2),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.PRODUCTOS IS 'Catálogo de productos';

-- ============================================
-- TABLA: STOCK
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.STOCK (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES siga_saas.PRODUCTOS(id) ON DELETE CASCADE,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    cantidad INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    cantidad_minima INTEGER DEFAULT 0 CHECK (cantidad_minima >= 0),
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(producto_id, local_id)
);

COMMENT ON TABLE siga_saas.STOCK IS 'Stock por producto y local';
CREATE INDEX idx_stock_producto ON siga_saas.STOCK(producto_id);
CREATE INDEX idx_stock_local ON siga_saas.STOCK(local_id);

-- ============================================
-- TABLA: MOVIMIENTOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.MOVIMIENTOS (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER REFERENCES siga_saas.PRODUCTOS(id) ON DELETE CASCADE,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'VENTA', 'AJUSTE', 'TRASLADO')),
    cantidad INTEGER NOT NULL,
    cantidad_anterior INTEGER,
    cantidad_nueva INTEGER,
    usuario_id INTEGER REFERENCES siga_saas.USUARIOS(id),
    venta_id INTEGER, -- Referencia a VENTAS (se agregará después)
    observaciones TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.MOVIMIENTOS IS 'Historial de movimientos de stock';
CREATE INDEX idx_movimientos_producto ON siga_saas.MOVIMIENTOS(producto_id);
CREATE INDEX idx_movimientos_local ON siga_saas.MOVIMIENTOS(local_id);
CREATE INDEX idx_movimientos_fecha ON siga_saas.MOVIMIENTOS(fecha);

-- ============================================
-- TABLA: VENTAS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.VENTAS (
    id SERIAL PRIMARY KEY,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    usuario_id INTEGER REFERENCES siga_saas.USUARIOS(id),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA', 'CANCELADA', 'PENDIENTE')),
    observaciones TEXT
);

COMMENT ON TABLE siga_saas.VENTAS IS 'Registro de ventas (desde POS y manuales)';
CREATE INDEX idx_ventas_local ON siga_saas.VENTAS(local_id);
CREATE INDEX idx_ventas_usuario ON siga_saas.VENTAS(usuario_id);
CREATE INDEX idx_ventas_fecha ON siga_saas.VENTAS(fecha);

-- ============================================
-- TABLA: DETALLES_VENTA
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.DETALLES_VENTA (
    id SERIAL PRIMARY KEY,
    venta_id INTEGER REFERENCES siga_saas.VENTAS(id) ON DELETE CASCADE,
    producto_id INTEGER REFERENCES siga_saas.PRODUCTOS(id) ON DELETE CASCADE,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0)
);

COMMENT ON TABLE siga_saas.DETALLES_VENTA IS 'Detalles de cada venta (productos vendidos)';
CREATE INDEX idx_detalles_venta ON siga_saas.DETALLES_VENTA(venta_id);
CREATE INDEX idx_detalles_producto ON siga_saas.DETALLES_VENTA(producto_id);

-- Agregar foreign key de venta_id en MOVIMIENTOS
ALTER TABLE siga_saas.MOVIMIENTOS 
ADD CONSTRAINT fk_movimientos_venta 
FOREIGN KEY (venta_id) REFERENCES siga_saas.VENTAS(id) ON DELETE SET NULL;

-- ============================================
-- TABLA: ALERTAS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.ALERTAS (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('STOCK_BAJO', 'STOCK_AGOTADO', 'VENTA_ALTA', 'MOVIMIENTO_SOSPECHOSO')),
    producto_id INTEGER REFERENCES siga_saas.PRODUCTOS(id) ON DELETE CASCADE,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.ALERTAS IS 'Alertas y notificaciones del sistema';
CREATE INDEX idx_alertas_producto ON siga_saas.ALERTAS(producto_id);
CREATE INDEX idx_alertas_local ON siga_saas.ALERTAS(local_id);
CREATE INDEX idx_alertas_leida ON siga_saas.ALERTAS(leida);

-- ============================================
-- TABLAS DEL MÓDULO POS
-- ============================================

-- ============================================
-- TABLA: METODOS_PAGO
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.METODOS_PAGO (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.METODOS_PAGO IS 'Métodos de pago disponibles (EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA)';

-- ============================================
-- TABLA: TURNOS_CAJA
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.TURNOS_CAJA (
    id SERIAL PRIMARY KEY,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    usuario_id INTEGER REFERENCES siga_saas.USUARIOS(id),
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP,
    monto_inicial DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (monto_inicial >= 0),
    monto_final DECIMAL(10,2) CHECK (monto_final >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO', 'CERRADO'))
);

COMMENT ON TABLE siga_saas.TURNOS_CAJA IS 'Turnos de caja por local y usuario';
CREATE INDEX idx_turnos_local ON siga_saas.TURNOS_CAJA(local_id);
CREATE INDEX idx_turnos_usuario ON siga_saas.TURNOS_CAJA(usuario_id);
CREATE INDEX idx_turnos_estado ON siga_saas.TURNOS_CAJA(estado);

-- ============================================
-- TABLA: TRANSACCIONES_POS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.TRANSACCIONES_POS (
    id SERIAL PRIMARY KEY,
    venta_id INTEGER REFERENCES siga_saas.VENTAS(id) ON DELETE CASCADE,
    turno_caja_id INTEGER REFERENCES siga_saas.TURNOS_CAJA(id) ON DELETE CASCADE,
    metodo_pago_id INTEGER REFERENCES siga_saas.METODOS_PAGO(id),
    monto DECIMAL(10,2) NOT NULL CHECK (monto >= 0),
    cambio DECIMAL(10,2) DEFAULT 0 CHECK (cambio >= 0),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA', 'CANCELADA', 'REEMBOLSADA'))
);

COMMENT ON TABLE siga_saas.TRANSACCIONES_POS IS 'Transacciones del punto de venta';
CREATE INDEX idx_transacciones_venta ON siga_saas.TRANSACCIONES_POS(venta_id);
CREATE INDEX idx_transacciones_turno ON siga_saas.TRANSACCIONES_POS(turno_caja_id);

-- ============================================
-- TABLA: CARRITO_POS (Opcional, para persistencia)
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.CARRITO_POS (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES siga_saas.USUARIOS(id) ON DELETE CASCADE,
    local_id INTEGER REFERENCES siga_saas.LOCALES(id) ON DELETE CASCADE,
    producto_id INTEGER REFERENCES siga_saas.PRODUCTOS(id) ON DELETE CASCADE,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.CARRITO_POS IS 'Carrito temporal del POS (puede limpiarse periódicamente)';
CREATE INDEX idx_carrito_usuario ON siga_saas.CARRITO_POS(usuario_id);
CREATE INDEX idx_carrito_local ON siga_saas.CARRITO_POS(local_id);
