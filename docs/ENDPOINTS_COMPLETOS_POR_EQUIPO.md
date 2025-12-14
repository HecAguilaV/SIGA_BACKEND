# Endpoints Completos por Equipo - SIGA Backend

**Base URL:** `https://siga-backend-production.up.railway.app`  
**Fecha de revisión:** 2025-01-XX  
**Revisión:** Código fuente completo verificado

---

## 🔴 CRÍTICO: Problemas Identificados

### 1. Stock
- **Endpoint real:** `POST /api/saas/stock` (crea O actualiza si existe)
- **NO existe:** `PUT /api/saas/stock/{id}` (algunos frontends pueden estar intentando esto)
- **Problema reportado:** Se agrega stock pero no se muestra. Verificar que el stock tenga `usuario_comercial_id` asignado.

### 2. Precios
- **Campo real:** `precioUnitario` (String, puede ser null)
- **NO existe:** `precio` (algunos frontends pueden estar buscando este campo)
- **Problema reportado:** No se visualizan precios. Verificar parsing en frontend.

---

## 📱 APP MÓVIL (Operativa)

**Base:** `/api/saas/*` y `/api/auth/*`  
**Autenticación:** JWT Bearer Token (obtenido de `/api/auth/login`)

### Autenticación
- `POST /api/auth/login` - Login operativo
  - **Request:** `{ "email": "string", "password": "string" }`
  - **Response:** `{ "success": true, "accessToken": "...", "refreshToken": "...", "user": { "id": 1, "email": "...", "nombre": "...", "apellido": "...", "rol": "...", "nombreEmpresa": "...", "localPorDefecto": { "id": 1, "nombre": "...", "ciudad": "..." } } }`
- `POST /api/auth/register` - Registro operativo
- `POST /api/auth/refresh` - Renovar token
- `GET /api/auth/me` - Obtener perfil actual (incluye nombreEmpresa)

### Productos
- `GET /api/saas/productos` - Listar productos activos
  - **Response:** `{ "success": true, "productos": [{ "id": 1, "nombre": "...", "descripcion": "...", "categoriaId": 1, "codigoBarras": "...", "precioUnitario": "1000.00", "activo": true, ... }], "total": 1 }`
  - **⚠️ Campo precio:** `precioUnitario` (String, puede ser null)
- `GET /api/saas/productos/{id}` - Obtener producto por ID
- `POST /api/saas/productos` - Crear producto
  - **Request:** `{ "nombre": "string", "descripcion": "string?", "categoriaId": int?, "codigoBarras": "string?", "precioUnitario": "string?" }`
- `PUT /api/saas/productos/{id}` - Actualizar producto
- `DELETE /api/saas/productos/{id}` - Eliminar producto (soft delete)

### Stock
- `GET /api/saas/stock` - Listar stock
  - **Query params:** `?localId=1` (opcional)
  - **Response:** `{ "success": true, "stock": [{ "id": 1, "producto_id": 1, "local_id": 1, "cantidad": 10, "min_stock": 5, "fecha_actualizacion": "..." }], "total": 1 }`
- `GET /api/saas/stock/{productoId}/{localId}` - Obtener stock específico
- `POST /api/saas/stock` - Crear o actualizar stock
  - **⚠️ IMPORTANTE:** Este endpoint CREA si no existe, o ACTUALIZA si ya existe
  - **Request:** `{ "productoId": 1, "localId": 1, "cantidad": 10, "cantidadMinima": 5 }`
  - **También acepta:** `{ "producto_id": 1, "local_id": 1, "cantidad": 10, "cantidad_minima": 5 }` o `{ "min_stock": 5 }`
  - **Response:** `{ "success": true, "message": "Stock actualizado exitosamente", "stock": { ... } }`
  - **❌ NO EXISTE:** `PUT /api/saas/stock/{id}`

### Locales
- `GET /api/saas/locales` - Listar locales activos
- `GET /api/saas/locales/{id}` - Obtener local por ID
- `POST /api/saas/locales` - Crear local
- `PUT /api/saas/locales/{id}` - Actualizar local
- `DELETE /api/saas/locales/{id}` - Eliminar local (soft delete)

### Categorías
- `GET /api/saas/categorias` - Listar categorías activas
- `GET /api/saas/categorias/{id}` - Obtener categoría por ID
- `POST /api/saas/categorias` - Crear categoría
- `PUT /api/saas/categorias/{id}` - Actualizar categoría
- `DELETE /api/saas/categorias/{id}` - Eliminar categoría (soft delete)

### Ventas
- `GET /api/saas/ventas` - Listar ventas
- `POST /api/saas/ventas` - Crear venta

### Usuarios Operativos
- `GET /api/saas/usuarios` - Listar usuarios (solo ADMINISTRADOR)
- `GET /api/saas/usuarios/{id}` - Obtener usuario por ID
- `POST /api/saas/usuarios` - Crear usuario
- `PUT /api/saas/usuarios/{id}` - Actualizar usuario (incluye reactivar: `{ "activo": true }`)
- `DELETE /api/saas/usuarios/{id}` - Desactivar usuario (soft delete)
- `GET /api/saas/usuarios/{id}/permisos` - Obtener permisos de usuario
- `POST /api/saas/usuarios/{id}/permisos` - Asignar permiso
- `DELETE /api/saas/usuarios/{id}/permisos/{codigoPermiso}` - Quitar permiso
- `GET /api/saas/usuarios/permisos/disponibles` - Listar permisos disponibles
- `PUT /api/saas/usuarios/{id}/empresa` - Asignar empresa a usuario
- `GET /api/saas/usuarios/sin-empresa` - Listar usuarios sin empresa

### Chat/Asistente IA
- `POST /api/saas/chat` - Enviar mensaje al asistente

---

## 💻 WEBAPP (Operativa)

**Base:** `/api/saas/*` y `/api/auth/*`  
**Autenticación:** JWT Bearer Token (obtenido de `/api/auth/login`)

**⚠️ IMPORTANTE:** Los endpoints de WebApp son **IDÉNTICOS** a los de App Móvil. Ambos usan la misma base `/api/saas/*`.

### Autenticación
- `POST /api/auth/login` - Login operativo
- `POST /api/auth/register` - Registro operativo
- `POST /api/auth/refresh` - Renovar token
- `GET /api/auth/me` - Obtener perfil actual

### Productos
- `GET /api/saas/productos` - Listar productos activos
- `GET /api/saas/productos/{id}` - Obtener producto por ID
- `POST /api/saas/productos` - Crear producto
- `PUT /api/saas/productos/{id}` - Actualizar producto
- `DELETE /api/saas/productos/{id}` - Eliminar producto

### Stock
- `GET /api/saas/stock` - Listar stock
- `GET /api/saas/stock/{productoId}/{localId}` - Obtener stock específico
- `POST /api/saas/stock` - Crear o actualizar stock
  - **⚠️ Mismo endpoint que App Móvil**

### Locales
- `GET /api/saas/locales` - Listar locales activos
- `GET /api/saas/locales/{id}` - Obtener local por ID
- `POST /api/saas/locales` - Crear local
- `PUT /api/saas/locales/{id}` - Actualizar local
- `DELETE /api/saas/locales/{id}` - Eliminar local

### Categorías
- `GET /api/saas/categorias` - Listar categorías activas
- `GET /api/saas/categorias/{id}` - Obtener categoría por ID
- `POST /api/saas/categorias` - Crear categoría
- `PUT /api/saas/categorias/{id}` - Actualizar categoría
- `DELETE /api/saas/categorias/{id}` - Eliminar categoría

### Ventas
- `GET /api/saas/ventas` - Listar ventas
- `POST /api/saas/ventas` - Crear venta

### Usuarios Operativos
- `GET /api/saas/usuarios` - Listar usuarios
- `GET /api/saas/usuarios/{id}` - Obtener usuario por ID
- `POST /api/saas/usuarios` - Crear usuario
- `PUT /api/saas/usuarios/{id}` - Actualizar usuario
- `DELETE /api/saas/usuarios/{id}` - Desactivar usuario
- `GET /api/saas/usuarios/{id}/permisos` - Obtener permisos
- `POST /api/saas/usuarios/{id}/permisos` - Asignar permiso
- `DELETE /api/saas/usuarios/{id}/permisos/{codigoPermiso}` - Quitar permiso
- `GET /api/saas/usuarios/permisos/disponibles` - Listar permisos disponibles
- `PUT /api/saas/usuarios/{id}/empresa` - Asignar empresa
- `GET /api/saas/usuarios/sin-empresa` - Listar usuarios sin empresa

### Chat/Asistente IA
- `POST /api/saas/chat` - Enviar mensaje al asistente

---

## 🌐 WEB COMERCIAL

**Base:** `/api/comercial/*`  
**Autenticación:** JWT Bearer Token (obtenido de `/api/comercial/auth/login`)

### Autenticación
- `POST /api/comercial/auth/login` - Login comercial
  - **Request:** `{ "email": "string", "password": "string" }`
  - **Response:** `{ "success": true, "accessToken": "...", "refreshToken": "...", "user": { "id": 1, "email": "...", "nombre": "...", "apellido": "...", "rut": "...", "telefono": "...", "nombreEmpresa": "..." } }`
- `POST /api/comercial/auth/register` - Registro comercial
- `POST /api/comercial/auth/refresh` - Renovar token
- `POST /api/comercial/auth/obtener-token-operativo` - Obtener token operativo (SSO)
  - **Request:** `{ "token": "string?" }` (opcional, también puede usar Authorization header)
- `POST /api/comercial/auth/reset-password` - Solicitar reset de contraseña
- `POST /api/comercial/auth/change-password` - Cambiar contraseña con token
- `PUT /api/comercial/auth/update-email` - Actualizar email
- `PUT /api/comercial/auth/perfil` - Actualizar perfil (incluye nombreEmpresa)
  - **Request:** `{ "nombre": "string?", "apellido": "string?", "rut": "string?", "telefono": "string?", "nombreEmpresa": "string?" }`

### Facturas
- `POST /api/comercial/facturas` - Crear factura
- `GET /api/comercial/facturas` - Listar facturas del usuario autenticado
- `GET /api/comercial/facturas/{id}` - Obtener factura por ID
- `GET /api/comercial/facturas/numero/{numero}` - Obtener factura por número

### Suscripciones
- `GET /api/comercial/suscripciones` - Listar suscripciones del usuario autenticado
- `POST /api/comercial/suscripciones` - Crear suscripción

### Planes
- `GET /api/comercial/planes` - Listar planes disponibles
- `GET /api/comercial/planes/{id}` - Obtener plan por ID

### Chat/Asistente IA (Público)
- `POST /api/comercial/chat` - Chat comercial público (consultas sobre planes, precios, características)
  - **⚠️ NO requiere autenticación**
  - **Request:** `{ "message": "string" }`
  - **Response:** `{ "success": true, "response": "string" }`
  - **Diferencia con `/api/saas/chat`:** Este es público y para consultas comerciales, el otro es operativo y requiere autenticación

---

## 🔧 OTROS

### Health Check
- `GET /health` - Estado del servidor

### Admin (si existe)
- `GET /api/admin/users` - Listar usuarios (admin)

---

## 📋 NOTAS IMPORTANTES

### Formato de Respuestas
- **Éxito:** `{ "success": true, "data": {...} }`
- **Error:** `{ "success": false, "message": "..." }`

### Autenticación
- Todos los endpoints de `/api/saas/*` requieren:
  1. JWT Bearer Token en header `Authorization: Bearer <token>`
  2. Suscripción activa (verificado automáticamente)

### Separación por Empresa
- Todos los endpoints filtran automáticamente por `usuario_comercial_id`
- Si un usuario no tiene empresa asignada, puede recibir error: "No se pudo determinar la empresa"

### Campos de Precio
- **Productos:** `precioUnitario` (String, puede ser null)
- **Stock:** No tiene precio, solo cantidad
- **Ventas:** `precioUnitario` en detalles de venta

### Stock
- **NO existe** `PUT /api/saas/stock/{id}`
- **Solo existe** `POST /api/saas/stock` que crea o actualiza según si existe
- Acepta tanto `camelCase` como `snake_case` en el request

---

## 🐛 PROBLEMAS REPORTADOS Y SOLUCIONES

### 1. Stock no se muestra después de agregar
**Causa posible:** El stock creado no tiene `usuario_comercial_id` asignado, o el usuario no tiene empresa asignada.

**Solución:**
1. Verificar que el usuario tenga `usuario_comercial_id` asignado (usar `GET /api/saas/usuarios/{id}`)
2. Verificar que el producto y local pertenezcan a la misma empresa
3. El endpoint `POST /api/saas/stock` debería asignar automáticamente el `usuario_comercial_id` del stock basado en el producto/local

### 2. Precios no se visualizan
**Causa:** Frontend busca campo `precio` pero backend retorna `precioUnitario`.

**Solución:** Frontend debe usar `producto.precioUnitario` (String) en lugar de `producto.precio`.

### 3. No se puede ajustar stock
**Causa posible:** Frontend intenta usar `PUT /api/saas/stock/{id}` que no existe.

**Solución:** Usar `POST /api/saas/stock` con el mismo `productoId` y `localId` para actualizar.

---

**Última actualización:** Revisión completa del código fuente - 2025-01-XX
