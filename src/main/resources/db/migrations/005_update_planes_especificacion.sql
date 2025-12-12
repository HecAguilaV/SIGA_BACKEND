-- ============================================
-- Script de Migración: Actualizar Planes según Especificación
-- ============================================
-- Basado en ESPECIFICACION_BACKEND_DEFINITIVA.md v2.0

-- ============================================
-- 1. ACTUALIZAR PLAN 1: Emprendedor → Kiosco (Freemium)
-- ============================================
UPDATE siga_comercial.PLANES 
SET 
    nombre = 'Kiosco',
    descripcion = 'Plan gratuito permanente. Ideal para pequeños negocios que comienzan.',
    precio_mensual = 0,
    precio_anual = 0,
    caracteristicas = '{"trial_gratis": false, "soporte": "email", "asistente_ia": true, "es_freemium": true}'::jsonb
WHERE id = 1 AND nombre = 'Emprendedor';

-- ============================================
-- 2. ACTUALIZAR PLAN 2: Emprendedor Pro (Límites)
-- ============================================
UPDATE siga_comercial.PLANES 
SET 
    limite_bodegas = 2,  -- Cambiar de 3 a 2 según especificación
    caracteristicas = '{"trial_gratis": true, "soporte": "prioritario", "asistente_ia": true, "reportes_avanzados": true}'::jsonb
WHERE id = 2 AND nombre = 'Emprendedor Pro';

-- ============================================
-- 3. ACTUALIZAR PLAN 3: Crecimiento (Precio y Límites)
-- ============================================
UPDATE siga_comercial.PLANES 
SET 
    precio_mensual = 1.9,  -- Cambiar de 1.5 a 1.9 según especificación
    precio_anual = 19.0,   -- Actualizar proporcionalmente
    limite_bodegas = NULL,  -- Ilimitado (NULL = sin límite)
    limite_usuarios = NULL, -- Ilimitado
    caracteristicas = '{"trial_gratis": true, "soporte": "prioritario", "asistente_ia": true, "reportes_avanzados": true, "api_access": true}'::jsonb
WHERE id = 3 AND nombre = 'Crecimiento';

-- ============================================
-- NOTAS:
-- ============================================
-- - Plan Kiosco ahora es freemium (precio 0)
-- - Emprendedor Pro tiene 2 bodegas (no 3)
-- - Crecimiento tiene precio 1.9 UF y límites ilimitados
-- - Características se mantienen en JSONB por ahora (migración a tabla separada en Fase 2)
