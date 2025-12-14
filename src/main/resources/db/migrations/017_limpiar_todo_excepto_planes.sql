-- ============================================
-- Script de Limpieza Completa: Eliminar Todo Excepto Planes
-- ============================================
-- Limpia TODOS los datos (operativos y comerciales) EXCEPTO planes
-- 
-- USO: Ejecutar cuando se quiere empezar completamente desde cero
-- ADVERTENCIA: Esto elimina usuarios comerciales, suscripciones y facturas
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

-- Eliminar permisos asignados (antes de usuarios)
DELETE FROM siga_saas.USUARIOS_PERMISOS;

-- Eliminar usuarios operativos
DELETE FROM siga_saas.USUARIOS;

-- Eliminar movimientos de stock
DELETE FROM siga_saas.MOVIMIENTOS;

-- ============================================
-- 2. ELIMINAR DATOS COMERCIALES (siga_comercial)
-- ============================================

-- Eliminar facturas
DELETE FROM siga_comercial.FACTURAS;

-- Eliminar pagos
DELETE FROM siga_comercial.PAGOS;

-- Eliminar suscripciones
DELETE FROM siga_comercial.SUSCRIPCIONES;

-- Eliminar usuarios comerciales
DELETE FROM siga_comercial.USUARIOS;

-- ============================================
-- 3. RESETEAR SECUENCIAS (opcional, para empezar IDs desde 1)
-- ============================================

-- Resetear secuencias de siga_saas
ALTER SEQUENCE IF EXISTS siga_saas.PRODUCTOS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.LOCALES_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.CATEGORIAS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.STOCK_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.VENTAS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.DETALLES_VENTA_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.USUARIOS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.USUARIOS_PERMISOS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_saas.MOVIMIENTOS_id_seq RESTART WITH 1;

-- Resetear secuencias de siga_comercial
ALTER SEQUENCE IF EXISTS siga_comercial.USUARIOS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_comercial.SUSCRIPCIONES_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_comercial.FACTURAS_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS siga_comercial.PAGOS_id_seq RESTART WITH 1;

-- ============================================
-- 4. MANTENER (NO ELIMINAR)
-- ============================================
-- ✅ siga_comercial.PLANES (planes disponibles)
-- ✅ Esquemas y estructura de tablas

COMMIT;

-- ============================================
-- VERIFICACIÓN
-- ============================================
-- Ejecutar después para verificar:
-- SELECT COUNT(*) FROM siga_saas.PRODUCTOS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_saas.LOCALES; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_saas.USUARIOS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_comercial.USUARIOS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_comercial.SUSCRIPCIONES; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_comercial.FACTURAS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_comercial.PLANES; -- Debe mantener los planes
