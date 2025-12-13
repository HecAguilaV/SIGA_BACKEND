# 📋 Guía Completa de Migraciones - SIGA Backend

## ⚠️ IMPORTANTE

**Este documento lista TODAS las migraciones que deben ejecutarse en orden para que el backend funcione correctamente.**

Si falta alguna migración, el backend fallará con errores como:
- "Tabla no existe"
- "Columna no existe"
- "Foreign key constraint fails"

---

## 📦 Orden de Ejecución de Migraciones

### 1. Esquemas Base
**Archivo:** `001_create_schemas.sql` (si existe) o crear manualmente
```sql
CREATE SCHEMA IF NOT EXISTS siga_saas;
CREATE SCHEMA IF NOT EXISTS siga_comercial;
```

### 2. Tablas Operativas (siga_saas)
**Archivo:** `002_create_siga_saas_tables.sql`
**Tablas creadas:**
- `USUARIOS` - Usuarios operativos (ADMINISTRADOR, OPERADOR, CAJERO)
- `PRODUCTOS` - Catálogo de productos
- `CATEGORIAS` - Categorías de productos
- `LOCALES` - Bodegas/sucursales
- `STOCK` - Inventario por local
- `VENTAS` - Registro de ventas
- `METODOS_PAGO` - Métodos de pago disponibles

### 3. Tablas Comerciales (siga_comercial)
**Archivo:** `003_create_siga_comercial_tables.sql`
**Tablas creadas:**
- `USUARIOS` - Usuarios comerciales (clientes)
- `PLANES` - Planes de suscripción
- `SUSCRIPCIONES` - Suscripciones activas
- `FACTURAS` - Facturas de compra

### 4. ⚠️ SISTEMA DE PERMISOS (CRÍTICO)
**Archivo:** `008_create_sistema_permisos.sql`
**Tablas creadas:**
- `PERMISOS` - Catálogo de permisos del sistema
- `ROLES_PERMISOS` - Permisos por defecto de cada rol
- `USUARIOS_PERMISOS` - Permisos adicionales por usuario

**⚠️ ESTA MIGRACIÓN ES CRÍTICA** - Sin ella, el sistema de permisos no funciona y el backend falla.

**Datos insertados:**
- 25 permisos base (PRODUCTOS_*, STOCK_*, VENTAS_*, etc.)
- Permisos por defecto para ADMINISTRADOR (todos)
- Permisos por defecto para OPERADOR
- Permisos por defecto para CAJERO

### 5. Datos Iniciales
**Archivo:** `004_insert_initial_data.sql`
**Datos insertados:**
- Planes de suscripción (Emprendedor Pro, Crecimiento)
- Métodos de pago
- Categorías base

### 6. Campos Adicionales
**Archivo:** `006_add_campos_usuarios_comerciales.sql`
- Campos de trial (en_trial, fecha_inicio_trial, fecha_fin_trial)

**Archivo:** `012_add_nombre_empresa.sql`
- Campo `nombre_empresa` en usuarios comerciales

### 7. Correcciones (si aplica)
**Archivo:** `010_fix_facturas_schema.sql`
- Corrige esquema de tabla FACTURAS si está vacía

---

## ✅ Checklist de Verificación

Después de ejecutar todas las migraciones, verificar:

### Esquemas
- [ ] `siga_saas` existe
- [ ] `siga_comercial` existe

### Tablas Operativas (siga_saas)
- [ ] `USUARIOS`
- [ ] `PRODUCTOS`
- [ ] `CATEGORIAS`
- [ ] `LOCALES`
- [ ] `STOCK`
- [ ] `VENTAS`
- [ ] `METODOS_PAGO`
- [ ] **`PERMISOS`** ⬅️ CRÍTICO
- [ ] **`ROLES_PERMISOS`** ⬅️ CRÍTICO
- [ ] **`USUARIOS_PERMISOS`** ⬅️ CRÍTICO

### Tablas Comerciales (siga_comercial)
- [ ] `USUARIOS`
- [ ] `PLANES`
- [ ] `SUSCRIPCIONES`
- [ ] `FACTURAS`

### Datos
- [ ] Permisos insertados (25 permisos)
- [ ] Permisos por rol configurados
- [ ] Planes insertados (2 planes)
- [ ] Métodos de pago insertados

---

## 🔧 Script de Verificación

Ejecutar `VERIFICACION_TABLAS.sql` para verificar que todas las tablas existan:

```sql
-- Verificar tablas críticas
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'siga_saas' 
AND table_name IN ('PERMISOS', 'ROLES_PERMISOS', 'USUARIOS_PERMISOS');
```

Si no aparecen las 3 tablas, ejecutar `008_create_sistema_permisos.sql`.

---

## 🚨 Problemas Comunes

### Error: "Tabla siga_saas.permisos no existe"
**Solución:** Ejecutar `008_create_sistema_permisos.sql`

### Error: "Foreign key constraint fails"
**Solución:** Verificar que las tablas referenciadas existan (ej: USUARIOS debe existir antes de USUARIOS_PERMISOS)

### Error: "Column does not exist"
**Solución:** Verificar que se ejecutaron las migraciones de campos adicionales (006, 012)

---

## 📝 Notas

- Las migraciones deben ejecutarse en orden
- No ejecutar migraciones dos veces si ya se ejecutaron (usar `IF NOT EXISTS`)
- Hacer backup antes de ejecutar migraciones en producción
- Verificar con `VERIFICACION_TABLAS.sql` después de cada migración

---

## 🔗 Archivos Relacionados

- `src/main/resources/db/migrations/008_create_sistema_permisos.sql` - Sistema de permisos
- `src/main/resources/db/migrations/VERIFICACION_TABLAS.sql` - Script de verificación
- `src/main/resources/db/migrations/000_INICIALIZACION_COMPLETA.sql` - Guía de inicialización
