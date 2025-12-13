# 🎯 FUENTE DE VERDAD - SIGA Backend

**Documento Único y Definitivo**  
**Fecha:** 2025-01-XX  
**Versión:** 1.0

---

## ⚠️ IMPORTANTE

**Este es el ÚNICO documento que define qué tiene el backend y qué deben implementar los frontends.**

**NO hay otros documentos. Este es la verdad.**

---

## 📋 ¿QUÉ TIENE EL BACKEND?

### ✅ Endpoints Implementados y Funcionando

#### 🔐 Autenticación Comercial (`/api/comercial/auth`)
- ✅ `POST /api/comercial/auth/register` - Registro (incluye `nombreEmpresa`)
- ✅ `POST /api/comercial/auth/login` - Login
- ✅ `POST /api/comercial/auth/refresh` - Renovar token
- ✅ `PUT /api/comercial/auth/update-email` - Actualizar email (requiere auth + contraseña)
- ✅ `POST /api/comercial/auth/reset-password` - Solicitar reset (retorna token en MVP)
- ✅ `POST /api/comercial/auth/change-password` - Cambiar contraseña con token
- ✅ `POST /api/comercial/auth/obtener-token-operativo` - Obtener token para WebApp (SSO)

#### 🔐 Autenticación Operativa (`/api/auth`)
- ✅ `POST /api/auth/login` - Login usuarios operativos
- ✅ `POST /api/auth/register` - Registro usuarios operativos

#### 💬 Asistente IA
- ✅ `POST /api/comercial/chat` - Chat comercial (público, NO requiere auth)
- ✅ `POST /api/saas/chat` - Chat operativo (requiere auth + suscripción activa)

#### 📦 Gestión Operativa (`/api/saas`)
- ✅ `GET /api/saas/productos` - Listar productos
- ✅ `POST /api/saas/productos` - Crear producto (requiere permiso)
- ✅ `PUT /api/saas/productos/{id}` - Actualizar producto (requiere permiso)
- ✅ `DELETE /api/saas/productos/{id}` - Eliminar producto (requiere permiso)
- ✅ `GET /api/saas/locales` - Listar locales
- ✅ `POST /api/saas/locales` - Crear local (requiere permiso)
- ✅ `GET /api/saas/categorias` - Listar categorías
- ✅ `GET /api/saas/stock` - Ver stock
- ✅ `GET /api/saas/usuarios` - Listar usuarios operativos
- ✅ `GET /api/saas/usuarios/{id}/permisos` - Ver permisos de usuario
- ✅ `POST /api/saas/usuarios/{id}/permisos` - Asignar permiso a usuario

#### 💰 Portal Comercial (`/api/comercial`)
- ✅ `GET /api/comercial/planes` - Listar planes (público)
- ✅ `GET /api/comercial/planes/{id}` - Obtener plan (público)
- ✅ `GET /api/comercial/suscripciones` - Listar suscripciones del usuario
- ✅ `POST /api/comercial/suscripciones` - Crear suscripción (compra plan)
- ✅ `GET /api/comercial/facturas` - Listar facturas del usuario

#### 🏥 Healthcheck
- ✅ `GET /health` - Healthcheck para Railway

---

## 🗄️ Base de Datos

### ✅ Tablas Existentes

#### Esquema `siga_saas` (Operativo)
- ✅ `USUARIOS` - Usuarios operativos (ADMINISTRADOR, OPERADOR, CAJERO)
- ✅ `PRODUCTOS` - Catálogo de productos
- ✅ `CATEGORIAS` - Categorías de productos
- ✅ `LOCALES` - Bodegas/sucursales
- ✅ `STOCK` - Inventario por local
- ✅ `VENTAS` - Registro de ventas
- ✅ `PERMISOS` - Catálogo de permisos (26 permisos)
- ✅ `ROLES_PERMISOS` - Permisos por rol (40 registros)
- ✅ `USUARIOS_PERMISOS` - Permisos adicionales por usuario

#### Esquema `siga_comercial` (Comercial)
- ✅ `USUARIOS` - Usuarios comerciales (clientes)
- ✅ `PLANES` - Planes de suscripción (2 planes)
- ✅ `SUSCRIPCIONES` - Suscripciones activas
- ✅ `FACTURAS` - Facturas de compra

### ✅ Datos Iniciales
- ✅ 26 permisos insertados
- ✅ Permisos por rol configurados:
  - ADMINISTRADOR: 26 permisos (todos)
  - OPERADOR: 9 permisos
  - CAJERO: 5 permisos
- ✅ 2 planes insertados (Emprendedor Pro, Crecimiento)

---

## 🔐 Sistema de Permisos

### ✅ Funcionamiento
- ✅ `ADMINISTRADOR` tiene **todos los permisos automáticamente** (sin validar BD)
- ✅ `OPERADOR` y `CAJERO` validan permisos en BD (rol + adicionales)
- ✅ Permisos disponibles: 26 permisos (PRODUCTOS_*, STOCK_*, VENTAS_*, etc.)

---

## 💳 Planes y Suscripciones

### ✅ Planes Disponibles
1. **Emprendedor Pro** (ID: 2) - 0.9 UF/mes
   - 2 bodegas, 3 usuarios, 500 productos
   - Trial de 14 días automático

2. **Crecimiento** (ID: 3) - 1.9 UF/mes
   - Ilimitado (bodegas, usuarios, productos)
   - Trial de 14 días automático

### ✅ Trial
- ✅ Se activa automáticamente al comprar cualquier plan
- ✅ Duración: 14 días
- ✅ Durante el trial, acceso completo
- ✅ Después de 14 días, necesita pagar

---

## 🚫 ¿QUÉ NO TIENE EL BACKEND?

### ❌ NO Implementado
- ❌ Envío de emails (reset de contraseña retorna token en respuesta - MVP)
- ❌ Plan gratis permanente (eliminado, solo planes de pago con trial)
- ❌ Webhooks de pagos
- ❌ Notificaciones push
- ❌ Reportes avanzados (solo estructura básica)

---

## 📱 ¿QUÉ DEBEN IMPLEMENTAR LOS FRONTENDS?

### 🌐 Web Comercial

#### ✅ Debe Implementar
1. **Registro de usuario**
   - Campo `nombreEmpresa` (opcional)
   - Endpoint: `POST /api/comercial/auth/register`

2. **Actualizar email**
   - Formulario con contraseña actual
   - Endpoint: `PUT /api/comercial/auth/update-email`
   - ⚠️ Método: `PUT` (no POST)

3. **Reset de contraseña**
   - Flujo: Solicitar reset → Mostrar token → Cambiar contraseña
   - Endpoints: `POST /api/comercial/auth/reset-password` y `POST /api/comercial/auth/change-password`
   - ⚠️ En MVP, el token se retorna en la respuesta (no se envía por email)

4. **Asistente IA**
   - ⚠️ NO usar `VITE_GEMINI_API_KEY`
   - Usar endpoint: `POST /api/comercial/chat`
   - ⚠️ NO llamar directamente a Google Gemini API

5. **Compra de planes**
   - Mostrar 2 planes (Emprendedor Pro, Crecimiento)
   - Trial de 14 días se activa automáticamente
   - Endpoint: `POST /api/comercial/suscripciones`

#### ❌ NO Debe Implementar
- ❌ Login operativo (solo comercial)
- ❌ Llamadas directas a Gemini API
- ❌ Plan gratis (no existe)

---

### 🖥️ WebApp

#### ✅ Debe Implementar
1. **Login de usuarios operativos**
   - **ADMINISTRADOR (dueño):** Puede usar SSO desde Web Comercial O login directo
     - SSO: `POST /api/comercial/auth/obtener-token-operativo` (intercambia token comercial)
     - Login directo: `POST /api/auth/login` (email + password)
   - **OPERADOR / CAJERO (empleados):** Login directo obligatorio
     - Endpoint: `POST /api/auth/login`
     - Estos usuarios NO tienen cuenta comercial, solo operativa
     - Son creados por el ADMINISTRADOR desde WebApp: `POST /api/saas/usuarios`

2. **Sistema de permisos**
   - Consultar permisos: `GET /api/saas/usuarios/{id}/permisos`
   - ⚠️ ADMINISTRADOR tiene todos los permisos (no validar en frontend)
   - Validar permisos antes de mostrar acciones

3. **Gestión de productos**
   - Listar: `GET /api/saas/productos`
   - Crear: `POST /api/saas/productos` (requiere permiso)
   - Actualizar: `PUT /api/saas/productos/{id}` (requiere permiso)
   - Eliminar: `DELETE /api/saas/productos/{id}` (requiere permiso)

4. **Gestión de locales**
   - Listar: `GET /api/saas/locales`
   - Crear: `POST /api/saas/locales` (requiere permiso)

5. **Gestión de usuarios operativos**
   - Listar: `GET /api/saas/usuarios`
   - Crear: `POST /api/saas/usuarios` (solo ADMINISTRADOR puede crear OPERADOR/CAJERO)
   - Asignar permisos: `POST /api/saas/usuarios/{id}/permisos`

6. **Asistente IA operativo**
   - Endpoint: `POST /api/saas/chat`
   - Requiere: auth + suscripción activa

#### ❌ NO Debe Implementar
- ❌ Asumir que ADMINISTRADOR necesita permisos explícitos (el backend ya lo maneja)
- ❌ Asumir que OPERADOR/CAJERO pueden usar SSO (solo tienen login directo)

---

### 📱 App Móvil

#### ✅ Debe Implementar
1. **Login operativo**
   - Endpoint: `POST /api/auth/login`
   - Credenciales: email + password de usuario operativo

2. **Consultar permisos**
   - Endpoint: `GET /api/saas/usuarios/{id}/permisos`
   - Validar permisos antes de mostrar acciones

3. **Gestión básica**
   - Listar productos: `GET /api/saas/productos`
   - Ver stock: `GET /api/saas/stock`
   - Crear ventas: `POST /api/saas/ventas` (si existe)

#### ❌ NO Debe Implementar
- ❌ Login comercial (solo operativo)
- ❌ Compra de planes (solo en Web Comercial)

---

## 🔗 URLs del Backend

### Producción (Railway)
- **Base URL:** `https://siga-backend-production.up.railway.app`
- **Healthcheck:** `https://siga-backend-production.up.railway.app/health`
- **Swagger:** `https://siga-backend-production.up.railway.app/swagger-ui.html`

---

## ✅ Checklist de Verificación

### Para Web Comercial
- [ ] Campo `nombreEmpresa` en registro
- [ ] Actualizar email con método `PUT`
- [ ] Reset de contraseña (mostrar token en MVP)
- [ ] Asistente IA usando `/api/comercial/chat` (NO Gemini directo)
- [ ] Mostrar solo 2 planes (sin plan gratis)

### Para WebApp
- [ ] Login directo funcionando (`POST /api/auth/login`)
- [ ] SSO desde Web Comercial (opcional para ADMINISTRADOR)
- [ ] Intercambio de token operativo (si usa SSO)
- [ ] Validación de permisos (ADMINISTRADOR tiene todos)
- [ ] CRUD de productos con validación de permisos
- [ ] Crear usuarios OPERADOR/CAJERO desde WebApp
- [ ] Asistente IA usando `/api/saas/chat`

### Para App Móvil
- [ ] Login operativo funcionando
- [ ] Consulta de permisos
- [ ] Validación de permisos antes de acciones

---

## 🚨 Errores Comunes y Soluciones

### Error: "Tabla siga_saas.permisos no existe"
**Solución:** Ejecutar migración `008_create_sistema_permisos.sql` (ya ejecutada)

### Error: "403 Forbidden" para ADMINISTRADOR
**Solución:** Ya resuelto - ADMINISTRADOR tiene todos los permisos automáticamente

### Error: "404 Not Found" en update-email
**Solución:** Verificar que el método sea `PUT` (no POST) y que el código esté desplegado

### Error: "VITE_GEMINI_API_KEY no configurado"
**Solución:** NO usar Gemini directo. Usar endpoint `/api/comercial/chat` o `/api/saas/chat`

### Error: "Plan gratis no encontrado"
**Solución:** Plan gratis fue eliminado. Solo hay 2 planes de pago con trial.

---

## 📞 Soporte

**Si hay dudas o problemas:**
1. Verificar este documento primero
2. Verificar que el endpoint exista en la lista de arriba
3. Verificar que el método HTTP sea correcto
4. Verificar que la autenticación sea correcta

---

## 📝 Notas Finales

- **Este documento es la ÚNICA fuente de verdad**
- **Todo lo que está aquí está implementado y funcionando**
- **Todo lo que NO está aquí NO existe en el backend**
- **Si un endpoint no está listado, NO existe**

---

**Última actualización:** 2025-01-XX  
**Estado del Backend:** ✅ Completo y funcionando  
**Desplegado en:** Railway (producción)
