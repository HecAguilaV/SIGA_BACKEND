# 📊 Estado Completo del Backend - SIGA

## ✅ Definición Completa del Backend

Este documento define **completamente** el estado del backend para que todos los equipos vayan en la misma dirección.

---

## 🗄️ Base de Datos

### Esquemas Requeridos
- ✅ `siga_saas` - Sistema operativo (inventario, ventas, usuarios operativos)
- ✅ `siga_comercial` - Portal comercial (planes, suscripciones, facturas)

### Tablas Críticas (Deben Existir)

#### Esquema `siga_saas`
1. ✅ `USUARIOS` - Usuarios operativos (ADMINISTRADOR, OPERADOR, CAJERO)
2. ✅ `PRODUCTOS` - Catálogo de productos
3. ✅ `CATEGORIAS` - Categorías de productos
4. ✅ `LOCALES` - Bodegas/sucursales
5. ✅ `STOCK` - Inventario por local
6. ✅ `VENTAS` - Registro de ventas
7. ✅ `PERMISOS` ⬅️ **CRÍTICO** - Catálogo de permisos
8. ✅ `ROLES_PERMISOS` ⬅️ **CRÍTICO** - Permisos por rol
9. ✅ `USUARIOS_PERMISOS` ⬅️ **CRÍTICO** - Permisos adicionales por usuario

#### Esquema `siga_comercial`
1. ✅ `USUARIOS` - Usuarios comerciales (clientes)
2. ✅ `PLANES` - Planes de suscripción (2 planes: Emprendedor Pro, Crecimiento)
3. ✅ `SUSCRIPCIONES` - Suscripciones activas
4. ✅ `FACTURAS` - Facturas de compra

---

## 🔐 Sistema de Permisos

### Roles
- `ADMINISTRADOR` - Tiene **todos los permisos automáticamente** (sin validar BD)
- `OPERADOR` - Permisos por defecto (ver productos, crear productos, actualizar stock, etc.)
- `CAJERO` - Permisos limitados (ver productos, crear ventas, etc.)

### Permisos Disponibles (25 permisos)
- `PRODUCTOS_VER`, `PRODUCTOS_CREAR`, `PRODUCTOS_ACTUALIZAR`, `PRODUCTOS_ELIMINAR`
- `STOCK_VER`, `STOCK_ACTUALIZAR`
- `VENTAS_VER`, `VENTAS_CREAR`
- `LOCALES_VER`, `LOCALES_CREAR`, `LOCALES_ACTUALIZAR`, `LOCALES_ELIMINAR`
- `CATEGORIAS_VER`, `CATEGORIAS_CREAR`, `CATEGORIAS_ACTUALIZAR`, `CATEGORIAS_ELIMINAR`
- `USUARIOS_VER`, `USUARIOS_CREAR`, `USUARIOS_ACTUALIZAR`, `USUARIOS_ELIMINAR`, `USUARIOS_PERMISOS`
- `REPORTES_VER`, `COSTOS_VER`
- `ASISTENTE_USAR`, `ANALISIS_IA`, `ASISTENTE_CRUD`

### Validación
- `ADMINISTRADOR` → Siempre `true` (sin consultar BD)
- `OPERADOR` / `CAJERO` → Valida permisos en BD (rol + adicionales)

---

## 📋 Migraciones Requeridas

**Orden de ejecución:**
1. `001_create_schemas.sql`
2. `002_create_siga_saas_tables.sql`
3. `003_create_siga_comercial_tables.sql`
4. `004_insert_initial_data.sql`
5. `006_add_campos_usuarios_comerciales.sql`
6. **`008_create_sistema_permisos.sql`** ⬅️ **CRÍTICO - OBLIGATORIO**
7. `012_add_nombre_empresa.sql`

**Verificación:**
Ejecutar `VERIFICACION_TABLAS.sql` para verificar que todas las tablas existan.

---

## 🔌 Endpoints Principales

### Autenticación Comercial
- `POST /api/comercial/auth/register` - Registro (incluye `nombreEmpresa`)
- `POST /api/comercial/auth/login` - Login
- `PUT /api/comercial/auth/update-email` - Actualizar email
- `POST /api/comercial/auth/reset-password` - Solicitar reset (retorna token en MVP)
- `POST /api/comercial/auth/change-password` - Cambiar contraseña con token

### Autenticación Operativa
- `POST /api/auth/login` - Login usuarios operativos
- `POST /api/auth/register` - Registro usuarios operativos

### Asistente IA
- `POST /api/comercial/chat` - Chat comercial (público, no requiere auth)
- `POST /api/saas/chat` - Chat operativo (requiere auth + suscripción activa)

### Gestión Operativa (requiere auth + suscripción)
- `GET /api/saas/productos` - Listar productos
- `POST /api/saas/productos` - Crear producto (requiere permiso)
- `GET /api/saas/locales` - Listar locales
- `POST /api/saas/locales` - Crear local (requiere permiso)
- `GET /api/saas/categorias` - Listar categorías
- `GET /api/saas/usuarios/{id}/permisos` - Ver permisos de usuario

---

## 🎯 Planes y Suscripciones

### Planes Disponibles
1. **Emprendedor Pro** (ID: 2) - 0.9 UF/mes
   - 2 bodegas, 3 usuarios, 500 productos
   - Trial de 14 días automático

2. **Crecimiento** (ID: 3) - 1.9 UF/mes
   - Ilimitado (bodegas, usuarios, productos)
   - Trial de 14 días automático

### Trial
- Se activa automáticamente al comprar cualquier plan
- Duración: 14 días
- Durante el trial, el usuario tiene acceso completo
- Después de 14 días, necesita pagar para continuar

---

## ⚙️ Configuración

### Variables de Entorno (Railway)
- `DATASOURCE_URL` - URL de conexión a PostgreSQL
- `DATASOURCE_USERNAME` - Usuario de BD
- `DATASOURCE_PASSWORD` - Contraseña de BD
- `JWT_SECRET` - Secreto para firmar tokens JWT
- `GEMINI_API_KEY` - API key de Google Gemini

### Healthcheck
- Endpoint: `/health`
- Retorna: `{"status": "UP"}`

---

## ✅ Checklist de Verificación

Antes de considerar el backend "completo", verificar:

### Base de Datos
- [ ] Todos los esquemas existen
- [ ] Todas las tablas críticas existen
- [ ] Tabla `PERMISOS` existe (migración 008 ejecutada)
- [ ] Permisos insertados (25 permisos)
- [ ] Permisos por rol configurados

### Endpoints
- [ ] Autenticación comercial funciona
- [ ] Autenticación operativa funciona
- [ ] Asistente IA funciona (comercial y operativo)
- [ ] Endpoints de gestión operativa funcionan
- [ ] Sistema de permisos funciona (ADMINISTRADOR tiene todos)

### Funcionalidades
- [ ] Trial de 14 días se activa automáticamente
- [ ] Reset de contraseña funciona (retorna token en MVP)
- [ ] Actualización de email funciona
- [ ] Campo `nombreEmpresa` funciona

---

## 📚 Documentación

- `docs/MIGRACIONES_COMPLETAS.md` - Guía completa de migraciones
- `src/main/resources/db/migrations/VERIFICACION_TABLAS.sql` - Script de verificación
- `CHALLA/docs/INSTRUCCIONES_FRONTENDS_BREVE.md` - Instrucciones para frontends

---

## 🚨 Problemas Conocidos y Soluciones

### "Tabla siga_saas.permisos no existe"
**Solución:** Ejecutar `008_create_sistema_permisos.sql`

### "403 Forbidden" para ADMINISTRADOR
**Solución:** Ya resuelto - ADMINISTRADOR tiene todos los permisos automáticamente

### "404 Not Found" en update-email
**Solución:** Verificar que el método sea `PUT` (no POST) y que el código esté desplegado

---

**Última actualización:** 2025-01-XX
**Estado:** ✅ Backend completamente definido y documentado
