# Documentación Técnica de API - SIGA Backend

## Información General

**Base URL**: `https://siga-backend-production.up.railway.app`  
**Versión**: 1.0.0  
**Formato**: JSON  
**Autenticación**: JWT Bearer Token

## Autenticación

### Flujo de Autenticación

1. **Registro/Login**: Obtener tokens de acceso
2. **Uso de API**: Incluir token en header `Authorization: Bearer <token>`
3. **Refresh**: Renovar tokens expirados

### Headers Requeridos

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

## Endpoints

### Health Check

**GET** `/health`

Verifica el estado del servidor y la conexión a la base de datos.

**Respuesta**:
```json
{
  "status": "healthy",
  "database": "connected",
  "timestamp": "2025-12-03T10:00:00Z"
}
```

### Autenticación

#### Registro de Usuario

**POST** `/api/auth/register`

Registra un nuevo usuario en el sistema.

**Request Body**:
```json
{
  "email": "usuario@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "rol": "OPERADOR"
}
```

**Roles válidos**: `ADMINISTRADOR`, `OPERADOR`, `CAJERO`

**Respuesta** (201 Created):
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "OPERADOR"
  }
}
```

#### Login

**POST** `/api/auth/login`

Autentica un usuario existente.

**Request Body**:
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Respuesta** (200 OK):
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
    "rol": "OPERADOR"
  }
}
```

#### Refresh Token

**POST** `/api/auth/refresh`

Renueva los tokens de acceso.

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta** (200 OK):
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Productos

#### Listar Productos

**GET** `/api/saas/productos`

Lista todos los productos activos del usuario autenticado.

**Headers**: `Authorization: Bearer <token>`

**Respuesta** (200 OK):
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

#### Obtener Producto

**GET** `/api/saas/productos/{id}`

Obtiene un producto específico por ID.

**Headers**: `Authorization: Bearer <token>`

**Respuesta** (200 OK):
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

#### Crear Producto

**POST** `/api/saas/productos`

Crea un nuevo producto. Requiere rol `ADMINISTRADOR`.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "nombre": "Nuevo Producto",
  "descripcion": "Descripción",
  "categoriaId": 1,
  "codigoBarras": "987654321",
  "precioUnitario": "1500.75"
}
```

**Respuesta** (201 Created):
```json
{
  "success": true,
  "message": "Producto creado exitosamente",
  "producto": {
    "id": 2,
    "nombre": "Nuevo Producto",
    "descripcion": "Descripción",
    "categoriaId": 1,
    "codigoBarras": "987654321",
    "precioUnitario": "1500.75",
    "activo": true,
    "fechaCreacion": "2025-12-03T10:00:00Z",
    "fechaActualizacion": "2025-12-03T10:00:00Z"
  }
}
```

#### Actualizar Producto

**PUT** `/api/saas/productos/{id}`

Actualiza un producto existente. Requiere rol `ADMINISTRADOR`.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "nombre": "Producto Actualizado",
  "descripcion": "Nueva descripción",
  "categoriaId": 2,
  "codigoBarras": "111222333",
  "precioUnitario": "2000.00"
}
```

**Respuesta** (200 OK):
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

#### Eliminar Producto

**DELETE** `/api/saas/productos/{id}`

Elimina un producto (soft delete). Requiere rol `ADMINISTRADOR`.

**Headers**: `Authorization: Bearer <token>`

**Respuesta** (200 OK):
```json
{
  "success": true,
  "message": "Producto eliminado exitosamente"
}
```

### Stock

#### Listar Stock

**GET** `/api/saas/stock?localId={localId}`

Lista el stock disponible. El parámetro `localId` es opcional.

**Headers**: `Authorization: Bearer <token>`

**Respuesta** (200 OK):
```json
{
  "success": true,
  "stock": [
    {
      "productoId": 1,
      "localId": 1,
      "cantidad": 100,
      "cantidadMinima": 10
    }
  ]
}
```

#### Actualizar Stock

**POST** `/api/saas/stock`

Actualiza el stock de un producto en un local.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "productoId": 1,
  "localId": 1,
  "cantidad": 150,
  "cantidadMinima": 15
}
```

**Respuesta** (200 OK):
```json
{
  "success": true,
  "message": "Stock actualizado exitosamente"
}
```

### Ventas

#### Listar Ventas

**GET** `/api/saas/ventas`

Lista todas las ventas del usuario autenticado.

**Headers**: `Authorization: Bearer <token>`

**Respuesta** (200 OK):
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
  ]
}
```

#### Crear Venta

**POST** `/api/saas/ventas`

Crea una nueva venta.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
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

**Respuesta** (201 Created):
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

### Planes

#### Listar Planes

**GET** `/api/comercial/planes`

Lista todos los planes de suscripción disponibles. Endpoint público.

**Respuesta** (200 OK):
```json
{
  "success": true,
  "planes": [
    {
      "id": 1,
      "nombre": "Emprendedor",
      "precio": "9900",
      "periodo": "mensual",
      "caracteristicas": ["Gestión de inventario", "Hasta 100 productos"]
    }
  ]
}
```

#### Obtener Plan

**GET** `/api/comercial/planes/{id}`

Obtiene un plan específico por ID. Endpoint público.

**Respuesta** (200 OK):
```json
{
  "success": true,
  "plan": {
    "id": 1,
    "nombre": "Emprendedor",
    "precio": "9900",
    "periodo": "mensual",
    "caracteristicas": ["Gestión de inventario", "Hasta 100 productos"]
  }
}
```

### Suscripciones

#### Listar Suscripciones

**GET** `/api/comercial/suscripciones`

Lista las suscripciones del usuario autenticado.

**Headers**: `Authorization: Bearer <token>`

**Respuesta** (200 OK):
```json
{
  "success": true,
  "suscripciones": [
    {
      "id": 1,
      "planId": 1,
      "usuarioId": 1,
      "fechaInicio": "2025-12-01T00:00:00Z",
      "fechaFin": "2026-01-01T00:00:00Z",
      "activa": true
    }
  ]
}
```

#### Crear Suscripción

**POST** `/api/comercial/suscripciones`

Crea una nueva suscripción.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "planId": 1,
  "periodo": "mensual"
}
```

**Respuesta** (201 Created):
```json
{
  "success": true,
  "message": "Suscripción creada exitosamente",
  "suscripcion": {
    "id": 2,
    "planId": 1,
    "usuarioId": 1,
    "fechaInicio": "2025-12-03T10:00:00Z",
    "fechaFin": "2026-01-03T10:00:00Z",
    "activa": true
  }
}
```

### Chat IA

#### Chat Comercial

**POST** `/api/comercial/chat`

Endpoint público para consultas comerciales sobre planes y precios.

**Request Body**:
```json
{
  "message": "¿Qué incluye el plan Emprendedor?"
}
```

**Respuesta** (200 OK):
```json
{
  "success": true,
  "response": "El plan Emprendedor incluye gestión de inventario y hasta 100 productos...",
  "message": "Respuesta generada exitosamente"
}
```

#### Chat Operativo

**POST** `/api/saas/chat`

Endpoint protegido para consultas operativas sobre inventario y ventas. Requiere autenticación y suscripción activa.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "message": "¿Cuántos productos tengo en stock?"
}
```

**Respuesta** (200 OK):
```json
{
  "success": true,
  "response": "Actualmente tienes 150 productos en stock distribuidos en 3 locales...",
  "message": "Respuesta generada exitosamente"
}
```

## Códigos de Estado HTTP

- `200 OK`: Solicitud exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Solicitud inválida
- `401 Unauthorized`: No autenticado o token inválido
- `403 Forbidden`: No tiene permisos para la operación
- `404 Not Found`: Recurso no encontrado
- `409 Conflict`: Conflicto (ej: email duplicado)
- `402 Payment Required`: Suscripción inactiva
- `500 Internal Server Error`: Error del servidor

## Validaciones

### Autenticación
- Email debe ser válido y único
- Password mínimo 8 caracteres
- Rol debe ser uno de: `ADMINISTRADOR`, `OPERADOR`, `CAJERO`

### Productos
- Nombre es requerido
- Precio unitario debe ser positivo
- Categoría debe existir

### Stock
- Cantidad debe ser >= 0
- Cantidad mínima debe ser >= 0
- Producto y local deben existir

### Ventas
- Local debe existir
- Debe tener al menos un detalle
- Cantidad de productos debe ser > 0

## Swagger UI

Documentación interactiva disponible en:
```
https://siga-backend-production.up.railway.app/swagger-ui/index.html
```

## Rate Limiting

Actualmente no hay límites de tasa implementados. Se recomienda implementar en producción.

## Seguridad

- Tokens JWT con expiración de 24 horas (access) y 7 días (refresh)
- Contraseñas hasheadas con BCrypt
- CORS configurado para orígenes específicos
- Validación de suscripción activa para endpoints operativos

