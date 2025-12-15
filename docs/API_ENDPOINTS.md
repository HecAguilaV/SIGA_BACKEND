# API Endpoints - SIGA Backend

Documentación completa y actualizada de todos los endpoints del backend SIGA.

**Base URL:** `https://siga-backend-production.up.railway.app`  
**Versión:** 1.0.0  
**Formato:** JSON  
**Autenticación:** JWT Bearer Token

---

## Tabla de Contenidos

1. [Autenticación Operativa](#1-autenticacion-operativa)
2. [Autenticación Comercial](#2-autenticacion-comercial)
3. [Productos](#3-productos)
4. [Stock](#4-stock)
5. [Locales](#5-locales)
6. [Categorías](#6-categorias)
7. [Ventas](#7-ventas)
8. [Usuarios Operativos](#8-usuarios-operativos)
9. [Planes](#9-planes)
10. [Suscripciones](#10-suscripciones)
11. [Facturas](#11-facturas)
12. [Chat y Asistente IA](#12-chat-y-asistente-ia)
13. [Health Check](#13-health-check)
14. [Admin](#14-admin)

---

## 1. Autenticación Operativa

Base: `/api/auth/*`  
Autenticación: No requerida para login/register, requerida para `/me`

### POST /api/auth/login

Iniciar sesión como usuario operativo.

**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "ADMINISTRADOR",
    "nombreEmpresa": "Mi Empresa",
    "localPorDefecto": {
      "id": 1,
      "nombre": "Bodega Central",
      "ciudad": "Santiago"
    }
  }
}
```

**Errores:**
- 401: Credenciales inválidas
- 401: Usuario inactivo

---

### POST /api/auth/register

Registrar nuevo usuario operativo.

**Request:**
```json
{
  "email": "nuevo@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "rol": "OPERADOR"
}
```

**Roles válidos:** `ADMINISTRADOR`, `OPERADOR`, `CAJERO`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "nuevo@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "OPERADOR"
  }
}
```

**Errores:**
- 400: Rol inválido
- 409: Email ya registrado

---

### POST /api/auth/refresh

Renovar tokens de acceso.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Errores:**
- 401: Token inválido o expirado

---

### GET /api/auth/me

Obtener perfil del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "ADMINISTRADOR",
    "nombreEmpresa": "Mi Empresa",
    "localPorDefecto": {
      "id": 1,
      "nombre": "Bodega Central",
      "ciudad": "Santiago"
    }
  }
}
```

**Errores:**
- 401: No autenticado
- 404: Usuario no encontrado
- 401: Usuario inactivo

---

## 2. Autenticación Comercial

Base: `/api/comercial/auth/*`  
Autenticación: No requerida para login/register, requerida para otros endpoints

### POST /api/comercial/auth/login

Iniciar sesión como usuario comercial.

**Request:**
```json
{
  "email": "cliente@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "cliente@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rut": "12345678-9",
    "telefono": "+56912345678",
    "nombreEmpresa": "Mi Empresa"
  }
}
```

---

### POST /api/comercial/auth/register

Registrar nuevo usuario comercial.

**Request:**
```json
{
  "email": "nuevo@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "nombreEmpresa": "Mi Empresa"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "nuevo@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "nombreEmpresa": "Mi Empresa"
  }
}
```

---

### POST /api/comercial/auth/refresh

Renovar tokens de acceso comercial.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### POST /api/comercial/auth/obtener-token-operativo

Obtener token operativo desde token comercial (SSO).

**Headers:** `Authorization: Bearer <token_comercial>`  
**O Request Body:**
```json
{
  "token": "token_comercial_aqui"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "ADMINISTRADOR",
    "nombreEmpresa": "Mi Empresa"
  }
}
```

---

### POST /api/comercial/auth/reset-password

Solicitar reset de contraseña.

**Request:**
```json
{
  "email": "usuario@example.com"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Si el email existe, se enviará un correo con instrucciones"
}
```

---

### POST /api/comercial/auth/change-password

Cambiar contraseña con token de reset.

**Request:**
```json
{
  "token": "token_de_reset",
  "newPassword": "nueva_password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Contraseña actualizada exitosamente"
}
```

---

### PUT /api/comercial/auth/update-email

Actualizar email del usuario comercial.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "newEmail": "nuevo@example.com",
  "password": "password_actual"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Email actualizado exitosamente"
}
```

---

### PUT /api/comercial/auth/perfil

Actualizar perfil del usuario comercial.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "rut": "12345678-9",
  "telefono": "+56912345678",
  "nombreEmpresa": "Mi Empresa Actualizada"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Perfil actualizado exitosamente",
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rut": "12345678-9",
    "telefono": "+56912345678",
    "nombreEmpresa": "Mi Empresa Actualizada"
  }
}
```

---

## 3. Productos

Base: `/api/saas/productos`  
Autenticación: Requerida + Suscripción activa  
Permisos: Ver (todos), Crear/Actualizar/Eliminar (solo ADMINISTRADOR)

### GET /api/saas/productos

Listar productos activos de la empresa del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "productos": [
    {
      "id": 1,
      "nombre": "Producto Ejemplo",
      "descripcion": "Descripción del producto",
      "categoriaId": 1,
      "codigoBarras": "123456789",
      "precioUnitario": "1000.50",
      "activo": true,
      "fechaCreacion": "2025-12-01T10:00:00Z",
      "fechaActualizacion": "2025-12-01T10:00:00Z"
    }
  ],
  "total": 1
}
```

**Nota:** El campo de precio es `precioUnitario` (String, puede ser null), NO `precio`.

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva

---

### GET /api/saas/productos/{id}

Obtener producto específico por ID.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "producto": {
    "id": 1,
    "nombre": "Producto Ejemplo",
    "descripcion": "Descripción del producto",
    "categoriaId": 1,
    "codigoBarras": "123456789",
    "precioUnitario": "1000.50",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z",
    "fechaActualizacion": "2025-12-01T10:00:00Z"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 404: Producto no encontrado

---

### POST /api/saas/productos

Crear nuevo producto. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Nuevo Producto",
  "descripcion": "Descripción opcional",
  "categoriaId": 1,
  "codigoBarras": "987654321",
  "precioUnitario": "1500.75"
}
```

**Campos requeridos:** `nombre`  
**Campos opcionales:** `descripcion`, `categoriaId`, `codigoBarras`, `precioUnitario`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Producto creado exitosamente",
  "producto": {
    "id": 2,
    "nombre": "Nuevo Producto",
    "descripcion": "Descripción opcional",
    "categoriaId": 1,
    "codigoBarras": "987654321",
    "precioUnitario": "1500.75",
    "activo": true,
    "fechaCreacion": "2025-12-03T10:00:00Z",
    "fechaActualizacion": "2025-12-03T10:00:00Z"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 403: No tiene permisos (solo ADMINISTRADOR)
- 400: Validación fallida

---

### PUT /api/saas/productos/{id}

Actualizar producto existente. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Producto Actualizado",
  "descripcion": "Nueva descripción",
  "categoriaId": 2,
  "codigoBarras": "111222333",
  "precioUnitario": "2000.00"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Producto actualizado exitosamente",
  "producto": {
    "id": 1,
    "nombre": "Producto Actualizado",
    "descripcion": "Nueva descripción",
    "categoriaId": 2,
    "codigoBarras": "111222333",
    "precioUnitario": "2000.00",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z",
    "fechaActualizacion": "2025-12-03T10:00:00Z"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 403: No tiene permisos
- 404: Producto no encontrado
- 400: Validación fallida

---

### DELETE /api/saas/productos/{id}

Eliminar producto (soft delete). Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Producto eliminado exitosamente"
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 403: No tiene permisos
- 404: Producto no encontrado

---

## 4. Stock

Base: `/api/saas/stock`  
Autenticación: Requerida + Suscripción activa  
Permisos: Ver (todos), Actualizar (OPERADOR y ADMINISTRADOR)

### GET /api/saas/stock

Listar stock de la empresa del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`  
**Query Params (opcional):** `?localId=1`

**Response (200 OK):**
```json
{
  "success": true,
  "stock": [
    {
      "id": 1,
      "producto_id": 1,
      "local_id": 1,
      "cantidad": 100,
      "cantidad_minima": 10,
      "fecha_actualizacion": "2025-12-03T10:00:00Z"
    }
  ],
  "total": 1
}
```

**Nota:** También acepta `productoId` y `localId` en camelCase en algunos contextos.

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva

---

### GET /api/saas/stock/{productoId}/{localId}

Obtener stock específico de un producto en un local.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "stock": {
    "id": 1,
    "producto_id": 1,
    "local_id": 1,
    "cantidad": 100,
    "cantidad_minima": 10,
    "fecha_actualizacion": "2025-12-03T10:00:00Z"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 404: Stock no encontrado

---

### POST /api/saas/stock

Crear o actualizar stock. Si el stock ya existe (mismo productoId y localId), se actualiza. Si no existe, se crea.

**IMPORTANTE:** NO existe `PUT /api/saas/stock/{id}`. Este es el único endpoint para crear/actualizar stock.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "productoId": 1,
  "localId": 1,
  "cantidad": 150,
  "cantidadMinima": 15
}
```

**También acepta snake_case:**
```json
{
  "producto_id": 1,
  "local_id": 1,
  "cantidad": 150,
  "cantidad_minima": 15
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Stock actualizado exitosamente",
  "stock": {
    "id": 1,
    "producto_id": 1,
    "local_id": 1,
    "cantidad": 150,
    "cantidad_minima": 15,
    "fecha_actualizacion": "2025-12-03T10:00:00Z"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 403: No tiene permisos
- 400: Validación fallida (producto o local no existe)
- 400: No se pudo determinar la empresa

---

## 5. Locales

Base: `/api/saas/locales`  
Autenticación: Requerida + Suscripción activa  
Permisos: Ver (todos), Crear/Actualizar/Eliminar (solo ADMINISTRADOR)

### GET /api/saas/locales

Listar locales activos de la empresa del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "locales": [
    {
      "id": 1,
      "nombre": "Bodega Central",
      "direccion": "Calle 123",
      "ciudad": "Santiago",
      "activo": true,
      "fechaCreacion": "2025-12-01T10:00:00Z"
    }
  ],
  "total": 1
}
```

---

### GET /api/saas/locales/{id}

Obtener local específico por ID.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "local": {
    "id": 1,
    "nombre": "Bodega Central",
    "direccion": "Calle 123",
    "ciudad": "Santiago",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z"
  }
}
```

---

### POST /api/saas/locales

Crear nuevo local. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Nuevo Local",
  "direccion": "Calle 456",
  "ciudad": "Valparaíso"
}
```

**Campos requeridos:** `nombre`  
**Campos opcionales:** `direccion`, `ciudad`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Local creado exitosamente",
  "local": {
    "id": 2,
    "nombre": "Nuevo Local",
    "direccion": "Calle 456",
    "ciudad": "Valparaíso",
    "activo": true,
    "fechaCreacion": "2025-12-03T10:00:00Z"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 403: No tiene permisos
- 400: No se pudo determinar la empresa

---

### PUT /api/saas/locales/{id}

Actualizar local existente. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Local Actualizado",
  "direccion": "Nueva dirección",
  "ciudad": "Concepción"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Local actualizado exitosamente",
  "local": {
    "id": 1,
    "nombre": "Local Actualizado",
    "direccion": "Nueva dirección",
    "ciudad": "Concepción",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z"
  }
}
```

---

### DELETE /api/saas/locales/{id}

Eliminar local (soft delete). Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Local eliminado exitosamente"
}
```

---

## 6. Categorías

Base: `/api/saas/categorias`  
Autenticación: Requerida + Suscripción activa  
Permisos: Ver (todos), Crear/Actualizar/Eliminar (solo ADMINISTRADOR)

### GET /api/saas/categorias

Listar categorías activas de la empresa del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "categorias": [
    {
      "id": 1,
      "nombre": "Bebidas",
      "descripcion": "Bebidas y refrescos",
      "activo": true,
      "fechaCreacion": "2025-12-01T10:00:00Z"
    }
  ],
  "total": 1
}
```

---

### GET /api/saas/categorias/{id}

Obtener categoría específica por ID.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "categoria": {
    "id": 1,
    "nombre": "Bebidas",
    "descripcion": "Bebidas y refrescos",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z"
  }
}
```

---

### POST /api/saas/categorias

Crear nueva categoría. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Nueva Categoría",
  "descripcion": "Descripción opcional"
}
```

**Campos requeridos:** `nombre`  
**Campos opcionales:** `descripcion`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Categoría creada exitosamente",
  "categoria": {
    "id": 2,
    "nombre": "Nueva Categoría",
    "descripcion": "Descripción opcional",
    "activo": true,
    "fechaCreacion": "2025-12-03T10:00:00Z"
  }
}
```

---

### PUT /api/saas/categorias/{id}

Actualizar categoría existente. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Categoría Actualizada",
  "descripcion": "Nueva descripción"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Categoría actualizada exitosamente",
  "categoria": {
    "id": 1,
    "nombre": "Categoría Actualizada",
    "descripcion": "Nueva descripción",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z"
  }
}
```

---

### DELETE /api/saas/categorias/{id}

Eliminar categoría (soft delete). Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Categoría eliminada exitosamente"
}
```

---

## 7. Ventas

Base: `/api/saas/ventas`  
Autenticación: Requerida + Suscripción activa  
Permisos: Ver (todos), Crear (CAJERO y ADMINISTRADOR)

### GET /api/saas/ventas

Listar ventas de la empresa del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "ventas": [
    {
      "id": 1,
      "localId": 1,
      "fechaVenta": "2025-12-03T10:00:00Z",
      "total": "5000.00",
      "detalles": [
        {
          "productoId": 1,
          "cantidad": 5,
          "precioUnitario": "1000.00"
        }
      ]
    }
  ],
  "total": 1
}
```

---

### POST /api/saas/ventas

Crear nueva venta.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "localId": 1,
  "detalles": [
    {
      "productoId": 1,
      "cantidad": 3,
      "precioUnitario": "1000.00"
    }
  ],
  "observaciones": "Venta al contado"
}
```

**Campos requeridos:** `localId`, `detalles` (array no vacío)  
**Campos opcionales:** `observaciones`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Venta creada exitosamente",
  "venta": {
    "id": 2,
    "localId": 1,
    "fechaVenta": "2025-12-03T10:00:00Z",
    "total": "3000.00"
  }
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 403: No tiene permisos
- 400: Validación fallida (local no existe, producto no existe, cantidad <= 0)

---

## 8. Usuarios Operativos

Base: `/api/saas/usuarios`  
Autenticación: Requerida + Suscripción activa  
Permisos: Solo ADMINISTRADOR

### GET /api/saas/usuarios

Listar usuarios operativos de la empresa del usuario autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "usuarios": [
    {
      "id": 1,
      "email": "usuario@example.com",
      "nombre": "Juan",
      "apellido": "Pérez",
      "rol": "OPERADOR",
      "activo": true,
      "fechaCreacion": "2025-12-01T10:00:00Z"
    }
  ],
  "total": 1
}
```

---

### GET /api/saas/usuarios/{id}

Obtener usuario operativo específico por ID.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "usuario": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "OPERADOR",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z"
  }
}
```

---

### POST /api/saas/usuarios

Crear nuevo usuario operativo. Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "email": "nuevo@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "rol": "OPERADOR"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Usuario creado exitosamente",
  "usuario": {
    "id": 2,
    "email": "nuevo@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "OPERADOR",
    "activo": true,
    "fechaCreacion": "2025-12-03T10:00:00Z"
  }
}
```

---

### PUT /api/saas/usuarios/{id}

Actualizar usuario operativo. Solo ADMINISTRADOR. Permite reactivar usuarios inactivos.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "nombre": "Juan Actualizado",
  "apellido": "Pérez",
  "rol": "ADMINISTRADOR",
  "activo": true
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Usuario actualizado exitosamente",
  "usuario": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan Actualizado",
    "apellido": "Pérez",
    "rol": "ADMINISTRADOR",
    "activo": true,
    "fechaCreacion": "2025-12-01T10:00:00Z"
  }
}
```

**Errores:**
- 403: No tiene acceso a este usuario (diferente empresa)

---

### DELETE /api/saas/usuarios/{id}

Desactivar usuario operativo (soft delete). Solo ADMINISTRADOR.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Usuario desactivado exitosamente"
}
```

---

### GET /api/saas/usuarios/{id}/permisos

Obtener permisos de un usuario operativo.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "permisos": [
    {
      "codigo": "PRODUCTOS_CREAR",
      "nombre": "Crear Productos",
      "categoria": "PRODUCTOS"
    }
  ],
  "total": 1
}
```

---

### POST /api/saas/usuarios/{id}/permisos

Asignar permiso adicional a un usuario operativo.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "codigoPermiso": "PRODUCTOS_CREAR"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Permiso asignado exitosamente"
}
```

---

### DELETE /api/saas/usuarios/{id}/permisos/{codigoPermiso}

Quitar permiso adicional de un usuario operativo.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Permiso eliminado exitosamente"
}
```

---

### GET /api/saas/usuarios/permisos/disponibles

Listar todos los permisos disponibles en el sistema.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "permisos": [
    {
      "codigo": "PRODUCTOS_CREAR",
      "nombre": "Crear Productos",
      "categoria": "PRODUCTOS"
    }
  ],
  "total": 25
}
```

---

### PUT /api/saas/usuarios/{id}/empresa

Asignar empresa a un usuario operativo sin empresa.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "usuarioComercialId": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Empresa asignada exitosamente"
}
```

---

### GET /api/saas/usuarios/sin-empresa

Listar usuarios operativos sin empresa asignada.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "usuarios": [
    {
      "id": 1,
      "email": "usuario@example.com",
      "nombre": "Juan",
      "apellido": "Pérez",
      "rol": "OPERADOR"
    }
  ],
  "total": 1
}
```

---

## 9. Planes

Base: `/api/comercial/planes`  
Autenticación: No requerida (público)

### GET /api/comercial/planes

Listar todos los planes de suscripción disponibles.

**Response (200 OK):**
```json
{
  "success": true,
  "planes": [
    {
      "id": 1,
      "nombre": "Emprendedor",
      "precio": "9900",
      "periodo": "mensual",
      "caracteristicas": {
        "productos_maximos": 100,
        "usuarios_maximos": 3,
        "trial_gratis": true
      }
    }
  ],
  "total": 2
}
```

---

### GET /api/comercial/planes/{id}

Obtener plan específico por ID.

**Response (200 OK):**
```json
{
  "success": true,
  "plan": {
    "id": 1,
    "nombre": "Emprendedor",
    "precio": "9900",
    "periodo": "mensual",
    "caracteristicas": {
      "productos_maximos": 100,
      "usuarios_maximos": 3,
      "trial_gratis": true
    }
  }
}
```

---

## 10. Suscripciones

Base: `/api/comercial/suscripciones`  
Autenticación: Requerida (usuario comercial)

### GET /api/comercial/suscripciones

Listar suscripciones del usuario comercial autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "suscripciones": [
    {
      "id": 1,
      "usuarioId": 1,
      "planId": 1,
      "fechaInicio": "2025-12-01",
      "fechaFin": "2026-01-01",
      "estado": "ACTIVA",
      "periodo": "MENSUAL"
    }
  ],
  "total": 1,
  "tieneSuscripcionActiva": true,
  "tieneTrialActivo": false,
  "enTrial": false,
  "fechaFinTrial": null
}
```

---

### POST /api/comercial/suscripciones

Crear nueva suscripción. Al crear una suscripción, se crea automáticamente el usuario operativo correspondiente.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "planId": 1,
  "periodo": "MENSUAL"
}
```

**Periodos válidos:** `MENSUAL`, `ANUAL`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Suscripción creada exitosamente",
  "suscripcion": {
    "id": 2,
    "usuarioId": 1,
    "planId": 1,
    "fechaInicio": "2025-12-03",
    "fechaFin": "2026-01-03",
    "estado": "ACTIVA",
    "periodo": "MENSUAL"
  }
}
```

---

## 11. Facturas

Base: `/api/comercial/facturas`  
Autenticación: Requerida (usuario comercial)

### POST /api/comercial/facturas

Crear nueva factura (usualmente al procesar un pago).

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "suscripcionId": 1,
  "monto": "9900",
  "metodoPago": "TRANSFERENCIA",
  "numeroTransaccion": "TXN123456"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "factura": {
    "id": 1,
    "numero": "FAC-2025-0001",
    "suscripcionId": 1,
    "monto": "9900",
    "metodoPago": "TRANSFERENCIA",
    "estado": "PAGADA",
    "fechaCreacion": "2025-12-03T10:00:00Z"
  }
}
```

---

### GET /api/comercial/facturas

Listar facturas del usuario comercial autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "facturas": [
    {
      "id": 1,
      "numero": "FAC-2025-0001",
      "suscripcionId": 1,
      "monto": "9900",
      "metodoPago": "TRANSFERENCIA",
      "estado": "PAGADA",
      "fechaCreacion": "2025-12-03T10:00:00Z"
    }
  ],
  "total": 1
}
```

---

### GET /api/comercial/facturas/{id}

Obtener factura específica por ID.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "factura": {
    "id": 1,
    "numero": "FAC-2025-0001",
    "suscripcionId": 1,
    "monto": "9900",
    "metodoPago": "TRANSFERENCIA",
    "estado": "PAGADA",
    "fechaCreacion": "2025-12-03T10:00:00Z"
  }
}
```

---

### GET /api/comercial/facturas/numero/{numero}

Obtener factura por número de factura.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "factura": {
    "id": 1,
    "numero": "FAC-2025-0001",
    "suscripcionId": 1,
    "monto": "9900",
    "metodoPago": "TRANSFERENCIA",
    "estado": "PAGADA",
    "fechaCreacion": "2025-12-03T10:00:00Z"
  }
}
```

---

## 12. Chat y Asistente IA

### POST /api/comercial/chat

Chat comercial público. No requiere autenticación. Para consultas sobre planes, precios y características.

**Request:**
```json
{
  "message": "¿Qué incluye el plan Emprendedor?"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "response": "El plan Emprendedor incluye gestión de inventario y hasta 100 productos...",
  "message": "Respuesta generada exitosamente"
}
```

---

### POST /api/saas/chat

Chat operativo protegido. Requiere autenticación y suscripción activa. Para consultas sobre inventario, ventas y operaciones.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "message": "¿Cuántos productos tengo en stock?"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "response": "Actualmente tienes 150 productos en stock distribuidos en 3 locales...",
  "message": "Respuesta generada exitosamente"
}
```

**Errores:**
- 401: No autenticado
- 402: Suscripción inactiva
- 429: Límite de rate excedido (Gemini API)
- 500: Error del servidor

---

## 13. Health Check

### GET /health

Verificar estado del servidor y conexión a la base de datos.

**Response (200 OK):**
```json
{
  "status": "healthy",
  "database": "connected",
  "timestamp": "2025-12-03T10:00:00Z"
}
```

---

## 14. Admin

Base: `/api/admin/*`  
Autenticación: Requerida (probablemente solo para administradores del sistema)

### GET /api/admin/users

Listar todos los usuarios del sistema (endpoint administrativo).

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "users": [
    {
      "id": 1,
      "email": "usuario@example.com",
      "nombre": "Juan",
      "rol": "ADMINISTRADOR"
    }
  ]
}
```

---

## Notas Importantes

### Autenticación

- Todos los endpoints de `/api/saas/*` requieren:
  1. JWT Bearer Token en header `Authorization: Bearer <token>`
  2. Suscripción activa (verificado automáticamente)

- Los endpoints de `/api/comercial/*` requieren autenticación excepto:
  - `GET /api/comercial/planes` (público)
  - `POST /api/comercial/chat` (público)

### Separación por Empresa

- Todos los endpoints operativos (`/api/saas/*`) filtran automáticamente por `usuario_comercial_id`
- Si un usuario no tiene empresa asignada, puede recibir error: "No se pudo determinar la empresa"
- El filtrado es transparente para el frontend

### Campos de Precio

- **Productos:** `precioUnitario` (String, puede ser null) - NO existe campo `precio`
- **Stock:** No tiene precio, solo cantidad
- **Ventas:** `precioUnitario` en detalles de venta

### Stock

- **NO existe** `PUT /api/saas/stock/{id}`
- **Solo existe** `POST /api/saas/stock` que crea o actualiza según si existe
- Acepta tanto `camelCase` como `snake_case` en el request

### Formato de Respuestas

- **Éxito:** `{ "success": true, "data": {...} }`
- **Error:** `{ "success": false, "message": "..." }`

### Códigos de Estado HTTP

- `200 OK`: Solicitud exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Solicitud inválida
- `401 Unauthorized`: No autenticado o token inválido
- `402 Payment Required`: Suscripción inactiva
- `403 Forbidden`: No tiene permisos para la operación
- `404 Not Found`: Recurso no encontrado
- `409 Conflict`: Conflicto (ej: email duplicado)
- `429 Too Many Requests`: Límite de rate excedido
- `500 Internal Server Error`: Error del servidor

---

**Última actualización:** Revisión completa del código fuente - 2025-12-15
