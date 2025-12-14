-- ============================================
-- Script de Limpieza: Eliminar Datos Operativos
-- ============================================
-- Limpia todos los datos operativos (productos, locales, stock, ventas, etc.)
-- MANTIENE: usuarios comerciales, planes, suscripciones, facturas
-- 
-- USO: Ejecutar cuando se quiere empezar desde cero con separación por empresa
-- ============================================

BEGIN;

-- ============================================
-- 1. ELIMINAR DATOS OPERATIVOS (siga_saas)
-- ============================================

-- Eliminar ventas y detalles de venta
DELETE FROM siga_saas.DETALLES_VENTA;
DELETE FROM siga_saas.VENTAS;

-- Eliminar stock
DELETE FROM siga_saas.STOCK;

-- Eliminar productos
DELETE FROM siga_saas.PRODUCTOS;

-- Eliminar categorías
DELETE FROM siga_saas.CATEGORIAS;

-- Eliminar locales
DELETE FROM siga_saas.LOCALES;

-- Eliminar usuarios operativos (se recrearán al crear suscripción)
DELETE FROM siga_saas.USUARIOS;

-- Eliminar movimientos de stock
DELETE FROM siga_saas.MOVIMIENTOS;

-- ============================================
-- 2. RESETEAR SECUENCIAS (opcional, para empezar IDs desde 1)
-- ============================================

-- Resetear secuencias de siga_saas
ALTER SEQUENCE IF EXISTS siga_saas.PRODUCTOS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.LOCALES_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.CATEGORIAS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.STOCK_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.VENTAS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.DETALLES_VENTA_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.USUARIOS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.MOVIMIENTOS_id_seq RESTART WITH 1;

-- ============================================
-- 3. MANTENER (NO ELIMINAR)
-- ============================================
-- ✅ siga_comercial.USUARIOS (usuarios comerciales/dueños)
-- ✅ siga_comercial.PLANES (planes disponibles)
-- ✅ siga_comercial.SUSCRIPCIONES (suscripciones activas)
-- ✅ siga_comercial.FACTURAS (historial de facturas)
-- ✅ siga_comercial.PAGOS (historial de pagos)

COMMIT;

-- ============================================
-- VERIFICACIÓN
-- ============================================
-- Ejecutar después para verificar:
-- SELECT COUNT(*) FROM siga_saas.PRODUCTOS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_saas.LOCALES; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_saas.USUARIOS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_comercial.USUARIOS; -- Debe mantener usuarios comerciales
