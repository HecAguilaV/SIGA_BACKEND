# 📦 Scripts de Migración de Base de Datos

Este directorio contiene los scripts SQL para crear y configurar la base de datos de SIGA.

## 📋 Orden de Ejecución

Los scripts deben ejecutarse en el siguiente orden:

1. **001_create_schemas.sql** - Crea los esquemas `siga_saas` y `siga_comercial`
2. **002_create_siga_saas_tables.sql** - Crea todas las tablas del esquema operativo
3. **003_create_siga_comercial_tables.sql** - Crea todas las tablas del esquema comercial
4. **004_insert_initial_data.sql** - Inserta datos iniciales (métodos de pago, planes, categorías)

## 🚀 Cómo Ejecutar los Scripts

### Opción 1: Desde Always Data (Recomendado)

1. Accede a tu panel de Always Data: https://admin.alwaysdata.com
2. Ve a **Bases de datos → PostgreSQL**
3. Selecciona tu base de datos `hector_siga_db`
4. Abre el **phpPgAdmin** o **pgAdmin** (si está disponible)
5. Ejecuta cada script en orden (001, 002, 003, 004)

### Opción 2: Desde Terminal (psql)

```bash
# Conectarte a la base de datos
psql -h postgresql-hector.alwaysdata.net -U hector -d hector_siga_db

# Ejecutar cada script
\i 001_create_schemas.sql
\i 002_create_siga_saas_tables.sql
\i 003_create_siga_comercial_tables.sql
\i 004_insert_initial_data.sql
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
WHERE table_schema = 'siga_saas';

-- Verificar tablas en siga_comercial
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'siga_comercial';

-- Verificar datos iniciales
SELECT * FROM siga_saas.METODOS_PAGO;
SELECT * FROM siga_comercial.PLANES;
SELECT * FROM siga_saas.CATEGORIAS;
```

## 📊 Estructura Creada

### Esquema `siga_saas` (Sistema Operativo)
- USUARIOS
- LOCALES
- USUARIOS_LOCALES
- CATEGORIAS
- PRODUCTOS
- STOCK
- MOVIMIENTOS
- VENTAS
- DETALLES_VENTA
- ALERTAS
- METODOS_PAGO (POS)
- TURNOS_CAJA (POS)
- TRANSACCIONES_POS (POS)
- CARRITO_POS (POS)

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

## 🔄 Próximos Pasos

Una vez ejecutados los scripts:
1. Verifica la conexión desde el backend
2. Crea modelos Exposed para interactuar con las tablas
3. Implementa los endpoints de la API
