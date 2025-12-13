-- ============================================
-- SCRIPT DE VERIFICACIÓN: Tablas Requeridas
-- ============================================
-- Ejecutar este script para verificar que todas las tablas necesarias existan
-- ============================================

-- ============================================
-- VERIFICAR ESQUEMAS
-- ============================================
SELECT 
    schema_name,
    CASE 
        WHEN schema_name IN ('siga_saas', 'siga_comercial') THEN '✅ Existe'
        ELSE '❌ FALTA'
    END as estado
FROM information_schema.schemata
WHERE schema_name IN ('siga_saas', 'siga_comercial')
ORDER BY schema_name;

-- ============================================
-- VERIFICAR TABLAS DEL ESQUEMA OPERATIVO (siga_saas)
-- ============================================
SELECT 
    table_name,
    CASE 
        WHEN table_name IN (
            'USUARIOS',
            'PRODUCTOS',
            'CATEGORIAS',
            'LOCALES',
            'STOCK',
            'VENTAS',
            'PERMISOS',           -- ⬅️ CRÍTICO
            'ROLES_PERMISOS',     -- ⬅️ CRÍTICO
            'USUARIOS_PERMISOS'   -- ⬅️ CRÍTICO
        ) THEN '✅ Existe'
        ELSE '⚠️ Opcional'
    END as estado,
    'siga_saas' as esquema
FROM information_schema.tables
WHERE table_schema = 'siga_saas'
ORDER BY 
    CASE 
        WHEN table_name IN ('PERMISOS', 'ROLES_PERMISOS', 'USUARIOS_PERMISOS') THEN 0
        ELSE 1
    END,
    table_name;

-- ============================================
-- VERIFICAR TABLAS DEL ESQUEMA COMERCIAL (siga_comercial)
-- ============================================
SELECT 
    table_name,
    CASE 
        WHEN table_name IN (
            'USUARIOS',
            'PLANES',
            'SUSCRIPCIONES',
            'FACTURAS'
        ) THEN '✅ Existe'
        ELSE '⚠️ Opcional'
    END as estado,
    'siga_comercial' as esquema
FROM information_schema.tables
WHERE table_schema = 'siga_comercial'
ORDER BY table_name;

-- ============================================
-- VERIFICAR TABLA PERMISOS (CRÍTICA)
-- ============================================
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_schema = 'siga_saas' 
            AND table_name = 'PERMISOS'
        ) THEN '✅ Tabla PERMISOS existe'
        ELSE '❌ ERROR: Tabla PERMISOS NO EXISTE - Ejecutar migración 008_create_sistema_permisos.sql'
    END as verificacion_permisos;

-- ============================================
-- VERIFICAR DATOS EN PERMISOS
-- ============================================
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_schema = 'siga_saas' 
            AND table_name = 'PERMISOS'
        ) THEN (
            SELECT COUNT(*) || ' permisos encontrados'
            FROM siga_saas.PERMISOS
        )
        ELSE 'Tabla no existe'
    END as cantidad_permisos;

-- ============================================
-- LISTAR PERMISOS EXISTENTES (si la tabla existe)
-- ============================================
SELECT 
    codigo,
    nombre,
    categoria,
    activo
FROM siga_saas.PERMISOS
ORDER BY categoria, codigo;

-- ============================================
-- VERIFICAR PERMISOS POR ROL
-- ============================================
SELECT 
    rp.rol,
    COUNT(rp.permiso_id) as cantidad_permisos
FROM siga_saas.ROLES_PERMISOS rp
GROUP BY rp.rol
ORDER BY rp.rol;
