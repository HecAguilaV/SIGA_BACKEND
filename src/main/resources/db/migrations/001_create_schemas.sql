-- ============================================
-- Script de Migración: Crear Esquemas
-- ============================================
-- Este script crea los esquemas siga_saas y siga_comercial
-- Ejecutar primero antes de crear las tablas

-- Crear esquema siga_saas (Sistema operativo)
CREATE SCHEMA IF NOT EXISTS siga_saas;

-- Crear esquema siga_comercial (Portal comercial)
CREATE SCHEMA IF NOT EXISTS siga_comercial;

-- Comentarios
COMMENT ON SCHEMA siga_saas IS 'Sistema operativo de gestión de inventario y ventas';
COMMENT ON SCHEMA siga_comercial IS 'Portal comercial y gestión de suscripciones';
