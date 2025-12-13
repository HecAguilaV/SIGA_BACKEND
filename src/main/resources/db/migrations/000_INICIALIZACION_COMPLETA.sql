-- ============================================
-- SCRIPT DE INICIALIZACIÓN COMPLETA - SIGA BACKEND
-- ============================================
-- Este script ejecuta TODAS las migraciones necesarias en orden
-- ⚠️ IMPORTANTE: Ejecutar en una base de datos limpia o verificar que no haya conflictos
-- ============================================

-- ============================================
-- PASO 1: Crear esquemas
-- ============================================
CREATE SCHEMA IF NOT EXISTS siga_saas;
CREATE SCHEMA IF NOT EXISTS siga_comercial;

-- ============================================
-- PASO 2: Tablas del esquema operativo (siga_saas)
-- ============================================
-- Ejecutar: 002_create_siga_saas_tables.sql
-- Esto crea: USUARIOS, PRODUCTOS, CATEGORIAS, LOCALES, STOCK, VENTAS, etc.

-- ============================================
-- PASO 3: Tablas del esquema comercial (siga_comercial)
-- ============================================
-- Ejecutar: 003_create_siga_comercial_tables.sql
-- Esto crea: USUARIOS, PLANES, SUSCRIPCIONES, FACTURAS, etc.

-- ============================================
-- PASO 4: Sistema de permisos (CRÍTICO)
-- ============================================
-- Ejecutar: 008_create_sistema_permisos.sql
-- Esto crea: PERMISOS, ROLES_PERMISOS, USUARIOS_PERMISOS
-- ⚠️ ESTE ES EL QUE FALTA Y CAUSA EL ERROR

-- ============================================
-- PASO 5: Datos iniciales
-- ============================================
-- Ejecutar: 004_insert_initial_data.sql
-- Esto inserta: Planes, métodos de pago, categorías base

-- ============================================
-- PASO 6: Campos adicionales (si aplica)
-- ============================================
-- Ejecutar: 006_add_campos_usuarios_comerciales.sql
-- Ejecutar: 012_add_nombre_empresa.sql

-- ============================================
-- VERIFICACIÓN FINAL
-- ============================================
-- Después de ejecutar todas las migraciones, verificar con:
-- SELECT table_name FROM information_schema.tables 
-- WHERE table_schema = 'siga_saas' ORDER BY table_name;

-- Tablas requeridas en siga_saas:
-- - USUARIOS
-- - PRODUCTOS
-- - CATEGORIAS
-- - LOCALES
-- - STOCK
-- - VENTAS
-- - PERMISOS ⬅️ CRÍTICO
-- - ROLES_PERMISOS ⬅️ CRÍTICO
-- - USUARIOS_PERMISOS ⬅️ CRÍTICO

-- Tablas requeridas en siga_comercial:
-- - USUARIOS
-- - PLANES
-- - SUSCRIPCIONES
-- - FACTURAS
