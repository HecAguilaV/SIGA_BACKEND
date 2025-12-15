# 📦 Scripts de Migración de Base de Datos

Este directorio contiene los scripts SQL esenciales para crear y configurar la base de datos de SIGA desde cero.

## 📋 Orden de Ejecución

Los scripts deben ejecutarse en el siguiente orden numérico:

1. **001_create_schemas.sql** - Crea los esquemas `siga_saas` y `siga_comercial`
2. **002_create_siga_saas_tables.sql** - Crea todas las tablas del esquema operativo
3. **003_create_siga_comercial_tables.sql** - Crea todas las tablas del esquema comercial
4. **004_insert_initial_data.sql** - Inserta datos iniciales (métodos de pago, planes, categorías)
5. **005_update_planes_especificacion.sql** - Actualiza especificaciones de planes
6. **006_add_campos_usuarios_comerciales.sql** - Agrega campos de trial a usuarios comerciales
7. **007_redisenar_tabla_facturas.sql** - Rediseña tabla de facturas
8. **008_create_sistema_permisos.sql** - ⚠️ **CRÍTICO** - Crea sistema de permisos (PERMISOS, ROLES_PERMISOS, USUARIOS_PERMISOS)
9. **010_fix_facturas_schema.sql** - Corrige esquema de facturas
10. **012_add_nombre_empresa.sql** - Agrega campo nombre_empresa a usuarios comerciales
11. **013_add_usuario_comercial_id.sql** - Agrega campo usuario_comercial_id para separación por empresa
12. **014_separacion_completa_por_empresa.sql** - Implementa separación completa por empresa

### ⚠️ MIGRACIÓN CRÍTICA: 008_create_sistema_permisos.sql

**Esta migración es OBLIGATORIA** - Sin ella, el backend fallará con errores como:
- "Tabla siga_saas.permisos no existe"
- "No property 'permiso' found"

**Tablas creadas:**
- `PERMISOS` - Catálogo de permisos (25 permisos base)
- `ROLES_PERMISOS` - Permisos por defecto de cada rol
- `USUARIOS_PERMISOS` - Permisos adicionales por usuario

## 🚀 Cómo Ejecutar los Scripts

### Opción 1: Desde Always Data (Recomendado)

1. Accede a tu panel de Always Data: https://admin.alwaysdata.com
2. Ve a **Bases de datos → PostgreSQL**
3. Selecciona tu base de datos `hector_siga_db`
4. Abre el **phpPgAdmin** o **pgAdmin** (si está disponible)
5. Ejecuta cada script en orden numérico (001, 002, 003, ...)

### Opción 2: Desde Terminal (psql)

```bash
# Conectarte a la base de datos
psql -h postgresql-hector.alwaysdata.net -U hector -d hector_siga_db

# Ejecutar cada script en orden
\i 001_create_schemas.sql
\i 002_create_siga_saas_tables.sql
\i 003_create_siga_comercial_tables.sql
\i 004_insert_initial_data.sql
\i 005_update_planes_especificacion.sql
\i 006_add_campos_usuarios_comerciales.sql
\i 007_redisenar_tabla_facturas.sql
\i 008_create_sistema_permisos.sql
\i 010_fix_facturas_schema.sql
\i 012_add_nombre_empresa.sql
\i 013_add_usuario_comercial_id.sql
\i 014_separacion_completa_por_empresa.sql
```

### Opción 3: Desde IntelliJ IDEA

1. Abre **View → Tool Windows → Database**
2. Conecta a tu base de datos PostgreSQL
3. Abre cada archivo `.sql` y ejecuta con `Ctrl+Enter` (o `Cmd+Enter` en Mac)

## ✅ Verificación

Después de ejecutar los scripts, verifica que todo esté correcto:

```sql
-- Verificar esquemas
SELECT schema_name FROM information_schema.schemata 
WHERE schema_name IN ('siga_saas', 'siga_comercial');

-- Verificar tablas en siga_saas
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'siga_saas'
ORDER BY table_name;

-- Verificar tablas en siga_comercial
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'siga_comercial'
ORDER BY table_name;

-- Verificar datos iniciales
SELECT * FROM siga_comercial.PLANES;
SELECT * FROM siga_saas.CATEGORIAS;
SELECT COUNT(*) FROM siga_saas.PERMISOS; -- Debe ser 25
```

## 📊 Estructura Creada

### Esquema `siga_saas` (Sistema Operativo)
- USUARIOS
- LOCALES
- CATEGORIAS
- PRODUCTOS
- STOCK
- MOVIMIENTOS
- VENTAS
- DETALLES_VENTA
- METODOS_PAGO (POS)
- TURNOS_CAJA (POS)
- TRANSACCIONES_POS (POS)
- CARRITO_POS (POS)
- **PERMISOS** ⬅️ CRÍTICO (migración 008)
- **ROLES_PERMISOS** ⬅️ CRÍTICO (migración 008)
- **USUARIOS_PERMISOS** ⬅️ CRÍTICO (migración 008)

### Esquema `siga_comercial` (Portal Comercial)
- USUARIOS
- PLANES
- SUSCRIPCIONES
- PAGOS
- FACTURAS
- CARRITOS

## ⚠️ Notas Importantes

- Los scripts usan `CREATE TABLE IF NOT EXISTS`, por lo que son idempotentes (puedes ejecutarlos múltiples veces)
- Los datos iniciales usan `ON CONFLICT DO NOTHING` para evitar duplicados
- Asegúrate de tener permisos suficientes en la base de datos
- Los scripts crean índices para mejorar el rendimiento de consultas frecuentes
- **Separación por empresa**: Todos los datos operativos están asociados a un `usuario_comercial_id` para multi-tenancy

## 🔄 Próximos Pasos

Una vez ejecutados los scripts:
1. Verifica la conexión desde el backend
2. Crea un usuario comercial desde Web Comercial
3. Crea una suscripción (esto crea automáticamente el usuario operativo)
4. Inicia sesión en WebApp o App Móvil
