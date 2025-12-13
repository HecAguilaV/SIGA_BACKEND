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

-- NOTA: Plan Kiosco (gratis) fue eliminado. Solo planes de pago con trial de 14 días.
INSERT INTO siga_comercial.PLANES (nombre, descripcion, precio_mensual, precio_anual, limite_bodegas, limite_usuarios, limite_productos, caracteristicas, orden) VALUES
    (
        'Emprendedor Pro',
        'Para negocios en crecimiento. Más bodegas y usuarios. Incluye trial de 14 días.',
        0.9, -- 0.9 UF mensual
        9.0, -- 9 UF anual
        2,   -- 2 bodegas
        3,   -- 3 usuarios
        500, -- 500 productos
        '{"trial_gratis": true, "soporte": "prioritario", "asistente_ia": true, "reportes_avanzados": true}'::jsonb,
        1
    ),
    (
        'Crecimiento',
        'Para empresas establecidas. Sin límites prácticos. Incluye trial de 14 días.',
        1.9, -- 1.9 UF mensual
        19.0, -- 19 UF anual
        NULL, -- Bodegas ilimitadas
        NULL, -- Usuarios ilimitados
        NULL, -- Sin límite de productos
        '{"trial_gratis": true, "soporte": "prioritario", "asistente_ia": true, "reportes_avanzados": true, "api_access": true}'::jsonb,
        2
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
