-- ============================================
-- Script: Limpiar datos de prueba
-- ============================================
-- Este script elimina datos de prueba pero MANTIENE:
-- - Estructura de tablas
-- - Planes (siga_comercial.PLANES)
-- - Métodos de pago (siga_saas.METODOS_PAGO)
-- - Categorías base (siga_saas.CATEGORIAS)
--
-- ⚠️ IMPORTANTE: Solo ejecutar en desarrollo/testing
-- NO ejecutar en producción con datos reales

-- ============================================
-- LIMPIAR ESQUEMA OPERATIVO (siga_saas)
-- ============================================

-- Eliminar relaciones primero (para evitar errores de FK)
DELETE FROM siga_saas.USUARIOS_LOCALES;
DELETE FROM siga_saas.USUARIOS_PERMISOS;
DELETE FROM siga_saas.ROLES_PERMISOS; -- Se recrean con migración 008
DELETE FROM siga_saas.MOVIMIENTOS;
DELETE FROM siga_saas.DETALLES_VENTA;
DELETE FROM siga_saas.VENTAS;
DELETE FROM siga_saas.STOCK;
DELETE FROM siga_saas.PRODUCTOS;
DELETE FROM siga_saas.LOCALES;
DELETE FROM siga_saas.USUARIOS WHERE email != 'admin.test@siga.com'; -- Mantener admin de prueba

-- ============================================
-- LIMPIAR ESQUEMA COMERCIAL (siga_comercial)
-- ============================================

DELETE FROM siga_comercial.FACTURAS;
DELETE FROM siga_comercial.PAGOS;
DELETE FROM siga_comercial.SUSCRIPCIONES;
DELETE FROM siga_comercial.CARRITOS;
DELETE FROM siga_comercial.USUARIOS WHERE email != 'admin.test@siga.com'; -- Mantener admin de prueba

-- ============================================
-- RESETEAR SECUENCIAS (para que IDs empiecen desde 1)
-- ============================================

-- Esquema operativo
ALTER SEQUENCE siga_saas.usuarios_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_saas.locales_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_saas.productos_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_saas.stock_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_saas.ventas_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_saas.detalles_venta_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_saas.movimientos_id_seq RESTART WITH 1;

-- Esquema comercial
ALTER SEQUENCE siga_comercial.usuarios_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_comercial.suscripciones_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_comercial.facturas_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_comercial.pagos_id_seq RESTART WITH 1;
ALTER SEQUENCE siga_comercial.carritos_id_seq RESTART WITH 1;

-- ============================================
-- VERIFICACIÓN
-- ============================================
-- Ejecutar después para verificar:
-- SELECT COUNT(*) FROM siga_saas.USUARIOS; -- Debe ser 1 (admin.test)
-- SELECT COUNT(*) FROM siga_saas.PRODUCTOS; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_saas.LOCALES; -- Debe ser 0
-- SELECT COUNT(*) FROM siga_comercial.USUARIOS; -- Debe ser 1 (admin.test)
-- SELECT COUNT(*) FROM siga_comercial.SUSCRIPCIONES; -- Debe ser 0
