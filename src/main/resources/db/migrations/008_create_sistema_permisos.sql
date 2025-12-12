-- ============================================
-- Script de Migración: Sistema de Permisos Granular
-- ============================================
-- Sistema flexible de permisos: Roles base + Permisos adicionales por usuario

-- ============================================
-- TABLA: PERMISOS
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.PERMISOS (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE siga_saas.PERMISOS IS 'Catálogo de permisos del sistema (operaciones disponibles)';
COMMENT ON COLUMN siga_saas.PERMISOS.codigo IS 'Código único del permiso (ej: PRODUCTOS_CREAR)';
COMMENT ON COLUMN siga_saas.PERMISOS.categoria IS 'Categoría del permiso (PRODUCTOS, STOCK, VENTAS, etc.)';

-- ============================================
-- TABLA: ROLES_PERMISOS (Permisos por defecto de cada rol)
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.ROLES_PERMISOS (
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'OPERADOR', 'CAJERO')),
    permiso_id INTEGER NOT NULL REFERENCES siga_saas.PERMISOS(id) ON DELETE CASCADE,
    PRIMARY KEY (rol, permiso_id)
);

COMMENT ON TABLE siga_saas.ROLES_PERMISOS IS 'Permisos por defecto de cada rol (plantillas base)';

-- ============================================
-- TABLA: USUARIOS_PERMISOS (Permisos adicionales por usuario)
-- ============================================
CREATE TABLE IF NOT EXISTS siga_saas.USUARIOS_PERMISOS (
    usuario_id INTEGER NOT NULL REFERENCES siga_saas.USUARIOS(id) ON DELETE CASCADE,
    permiso_id INTEGER NOT NULL REFERENCES siga_saas.PERMISOS(id) ON DELETE CASCADE,
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    asignado_por INTEGER REFERENCES siga_saas.USUARIOS(id),
    PRIMARY KEY (usuario_id, permiso_id)
);

COMMENT ON TABLE siga_saas.USUARIOS_PERMISOS IS 'Permisos adicionales asignados a usuarios específicos (más allá de su rol base)';
COMMENT ON COLUMN siga_saas.USUARIOS_PERMISOS.asignado_por IS 'ID del administrador que asignó el permiso';

-- ============================================
-- INSERTAR PERMISOS BASE
-- ============================================
INSERT INTO siga_saas.PERMISOS (codigo, nombre, descripcion, categoria) VALUES
-- Productos
('PRODUCTOS_VER', 'Ver Productos', 'Ver lista de productos', 'PRODUCTOS'),
('PRODUCTOS_CREAR', 'Crear Productos', 'Agregar nuevos productos al inventario', 'PRODUCTOS'),
('PRODUCTOS_ACTUALIZAR', 'Actualizar Productos', 'Modificar información de productos', 'PRODUCTOS'),
('PRODUCTOS_ELIMINAR', 'Eliminar Productos', 'Eliminar productos del sistema', 'PRODUCTOS'),
-- Stock
('STOCK_VER', 'Ver Stock', 'Ver niveles de stock', 'STOCK'),
('STOCK_ACTUALIZAR', 'Actualizar Stock', 'Modificar cantidades de stock', 'STOCK'),
-- Ventas
('VENTAS_VER', 'Ver Ventas', 'Ver historial de ventas', 'VENTAS'),
('VENTAS_CREAR', 'Crear Ventas', 'Registrar nuevas ventas', 'VENTAS'),
-- Locales
('LOCALES_VER', 'Ver Locales', 'Ver lista de locales', 'LOCALES'),
('LOCALES_CREAR', 'Crear Locales', 'Crear nuevos locales', 'LOCALES'),
('LOCALES_ACTUALIZAR', 'Actualizar Locales', 'Modificar información de locales', 'LOCALES'),
('LOCALES_ELIMINAR', 'Eliminar Locales', 'Eliminar locales', 'LOCALES'),
-- Categorías
('CATEGORIAS_VER', 'Ver Categorías', 'Ver categorías de productos', 'CATEGORIAS'),
('CATEGORIAS_CREAR', 'Crear Categorías', 'Crear nuevas categorías', 'CATEGORIAS'),
('CATEGORIAS_ACTUALIZAR', 'Actualizar Categorías', 'Modificar categorías', 'CATEGORIAS'),
('CATEGORIAS_ELIMINAR', 'Eliminar Categorías', 'Eliminar categorías', 'CATEGORIAS'),
-- Usuarios
('USUARIOS_VER', 'Ver Usuarios', 'Ver lista de usuarios operativos', 'USUARIOS'),
('USUARIOS_CREAR', 'Crear Usuarios', 'Crear nuevos usuarios operativos', 'USUARIOS'),
('USUARIOS_ACTUALIZAR', 'Actualizar Usuarios', 'Modificar usuarios operativos', 'USUARIOS'),
('USUARIOS_ELIMINAR', 'Eliminar Usuarios', 'Desactivar usuarios operativos', 'USUARIOS'),
('USUARIOS_PERMISOS', 'Asignar Permisos', 'Asignar permisos a usuarios', 'USUARIOS'),
-- Reportes
('REPORTES_VER', 'Ver Reportes', 'Ver reportes y gráficos', 'REPORTES'),
('COSTOS_VER', 'Ver Costos', 'Ver costos y facturación', 'REPORTES'),
-- Asistente IA
('ASISTENTE_USAR', 'Usar Asistente', 'Usar asistente IA básico', 'ASISTENTE'),
('ANALISIS_IA', 'Análisis IA', 'Solicitar análisis del asistente IA', 'ASISTENTE'),
('ASISTENTE_CRUD', 'CRUD por IA', 'Ejecutar operaciones CRUD por IA', 'ASISTENTE')
ON CONFLICT (codigo) DO NOTHING;

-- ============================================
-- PERMISOS POR DEFECTO: ADMINISTRADOR (TODOS)
-- ============================================
INSERT INTO siga_saas.ROLES_PERMISOS (rol, permiso_id)
SELECT 'ADMINISTRADOR', id FROM siga_saas.PERMISOS
ON CONFLICT DO NOTHING;

-- ============================================
-- PERMISOS POR DEFECTO: OPERADOR
-- ============================================
INSERT INTO siga_saas.ROLES_PERMISOS (rol, permiso_id)
SELECT 'OPERADOR', id FROM siga_saas.PERMISOS
WHERE codigo IN (
    'PRODUCTOS_VER',
    'PRODUCTOS_CREAR',
    'PRODUCTOS_ACTUALIZAR',
    'STOCK_VER',
    'STOCK_ACTUALIZAR',
    'LOCALES_VER',
    'CATEGORIAS_VER',
    'ASISTENTE_USAR',
    'ANALISIS_IA'
)
ON CONFLICT DO NOTHING;

-- ============================================
-- PERMISOS POR DEFECTO: CAJERO
-- ============================================
INSERT INTO siga_saas.ROLES_PERMISOS (rol, permiso_id)
SELECT 'CAJERO', id FROM siga_saas.PERMISOS
WHERE codigo IN (
    'PRODUCTOS_VER',
    'STOCK_VER',
    'VENTAS_CREAR',
    'VENTAS_VER',
    'ASISTENTE_USAR'
)
ON CONFLICT DO NOTHING;

-- ============================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ============================================
CREATE INDEX IF NOT EXISTS idx_permisos_codigo ON siga_saas.PERMISOS(codigo);
CREATE INDEX IF NOT EXISTS idx_permisos_categoria ON siga_saas.PERMISOS(categoria);
CREATE INDEX IF NOT EXISTS idx_roles_permisos_rol ON siga_saas.ROLES_PERMISOS(rol);
CREATE INDEX IF NOT EXISTS idx_usuarios_permisos_usuario ON siga_saas.USUARIOS_PERMISOS(usuario_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_permisos_permiso ON siga_saas.USUARIOS_PERMISOS(permiso_id);
