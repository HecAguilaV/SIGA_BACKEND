-- ============================================
-- Script de Migración: Datos Iniciales
-- ============================================
-- Insertar datos base necesarios para el funcionamiento del sistema

-- ============================================
-- MÉTODOS DE PAGO (siga_saas)
-- ============================================
INSERT INTO siga_saas.METODOS_PAGO (nombre, activo) VALUES
    ('EFECTIVO', true),
    ('TARJETA_DEBITO', true),
    ('TARJETA_CREDITO', true),
    ('TRANSFERENCIA', true)
ON CONFLICT DO NOTHING;

-- ============================================
-- PLANES DE SUSCRIPCIÓN (siga_comercial)
-- ============================================
-- Nota: Ajustar precios y características según necesidades reales

INSERT INTO siga_comercial.PLANES (nombre, descripcion, precio_mensual, precio_anual, limite_bodegas, limite_usuarios, limite_productos, caracteristicas, orden) VALUES
    (
        'Emprendedor',
        'Plan ideal para emprendedores que comienzan. Gestión básica de inventario.',
        0.5, -- 0.5 UF mensual
        5.0, -- 5 UF anual (2 meses gratis)
        1,   -- 1 bodega
        1,   -- 1 usuario
        100, -- 100 productos
        '{"trial_gratis": true, "soporte": "email", "asistente_ia": true}'::jsonb,
        1
    ),
    (
        'Emprendedor Pro',
        'Para negocios en crecimiento. Más bodegas y usuarios.',
        0.9, -- 0.9 UF mensual
        9.0, -- 9 UF anual
        3,   -- 3 bodegas
        3,   -- 3 usuarios
        500, -- 500 productos
        '{"trial_gratis": true, "soporte": "prioritario", "asistente_ia": true, "reportes_avanzados": true}'::jsonb,
        2
    ),
    (
        'Crecimiento',
        'Para empresas establecidas. Sin límites prácticos.',
        1.5, -- 1.5 UF mensual
        15.0, -- 15 UF anual
        10,  -- 10 bodegas
        10,  -- 10 usuarios
        NULL, -- Sin límite de productos
        '{"trial_gratis": true, "soporte": "prioritario", "asistente_ia": true, "reportes_avanzados": true, "api_access": true}'::jsonb,
        3
    )
ON CONFLICT (nombre) DO NOTHING;

-- ============================================
-- CATEGORÍAS BÁSICAS (siga_saas)
-- ============================================
INSERT INTO siga_saas.CATEGORIAS (nombre, descripcion, activa) VALUES
    ('Bebidas', 'Bebidas en general', true),
    ('Alimentos', 'Productos alimenticios', true),
    ('Limpieza', 'Productos de limpieza', true),
    ('Otros', 'Otras categorías', true)
ON CONFLICT (nombre) DO NOTHING;
