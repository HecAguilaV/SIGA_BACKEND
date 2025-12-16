-- ============================================
-- Script de Migración: Datos Iniciales
-- ============================================
-- Insertar datos base necesarios para el funcionamiento del sistema
-- NOTA: Categorías y productos se crean desde los frontends, no aquí

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
-- Planes disponibles para suscripción

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
