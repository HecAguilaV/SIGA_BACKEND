# API Documentation - SIGA Web Comercial

Documentación de endpoints para el frontend del portal comercial (siga.com).

## Base URL

- Desarrollo: `http://localhost:8080`
- Producción: `https://siga-backend-production.up.railway.app`

## Frontend URL

- Desarrollo: `http://localhost:5173` o `http://localhost:3000`
- Producción: `https://siga-web.vercel.app`

## Endpoints Públicos

### Autenticación

#### POST /api/auth/register

Registrar nuevo usuario en el portal comercial.

**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "rut": "12345678-9",
  "telefono": "+56912345678"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "usuario@example.com"
}
```

**Ejemplo con fetch:**
```javascript
// Reemplazar con la URL real de Railway cuando esté desplegado
const API_URL = 'https://siga-backend-production.up.railway.app';

const response = await fetch(`${API_URL}/api/auth/register`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'usuario@example.com',
    password: 'password123',
    nombre: 'Juan',
    apellido: 'Pérez'
  })
});

const data = await response.json();
```

#### POST /api/auth/login

Iniciar sesión.

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
  "message": "Login exitoso",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "usuario@example.com"
}
```

**Ejemplo con fetch:**
```javascript
const response = await fetch(`${API_URL}/api/auth/login`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'usuario@example.com',
    password: 'password123'
  })
});

const data = await response.json();
// Guardar tokens en localStorage
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);
```

#### POST /api/auth/refresh

Renovar token de acceso.

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
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Planes

#### GET /api/comercial/planes

Listar todos los planes disponibles (público, no requiere autenticación).

**Response (200 OK):**
```json
{
  "success": true,
  "planes": [
    {
      "id": 1,
      "nombre": "Plan Básico",
      "descripcion": "Ideal para pequeños negocios",
      "precioMensual": "9900",
      "precioAnual": "99000",
      "limiteBodegas": 1,
      "limiteUsuarios": 2,
      "limiteProductos": 1000,
      "activo": true
    }
  ],
  "total": 3
}
```

**Ejemplo con fetch:**
```javascript
const response = await fetch(`${API_URL}/api/comercial/planes`);
const data = await response.json();
console.log(data.planes);
```

#### GET /api/comercial/planes/{id}

Obtener un plan específico por ID.

**Ejemplo:**
```javascript
const response = await fetch(`${API_URL}/api/comercial/planes/1`);
const data = await response.json();
```

### Asistente Comercial

#### POST /api/comercial/chat

Chat con el asistente comercial (público, no requiere autenticación).

**Request:**
```json
{
  "message": "¿Qué incluye el plan Emprendedor Pro?"
}
```

**Response (200 OK):**
```json
{
  "response": "El plan Emprendedor Pro incluye...",
  "success": true
}
```

**Ejemplo con fetch:**
```javascript
const response = await fetch(`${API_URL}/api/comercial/chat`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    message: '¿Qué incluye el plan Emprendedor Pro?'
  })
});

const data = await response.json();
console.log(data.response);
```

## Endpoints Protegidos (Requieren Autenticación)

### Suscripciones

#### GET /api/comercial/suscripciones

Listar suscripciones del usuario autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "success": true,
  "suscripciones": [
    {
      "id": 1,
      "usuarioId": 1,
      "planId": 2,
      "fechaInicio": "2024-01-01",
      "fechaFin": "2024-02-01",
      "estado": "ACTIVA",
      "periodo": "MENSUAL"
    }
  ],
  "total": 1
}
```

**Ejemplo con fetch:**
```javascript
const token = localStorage.getItem('accessToken');
const response = await fetch(`${API_URL}/api/comercial/suscripciones`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
```

#### POST /api/comercial/suscripciones

Crear nueva suscripción.

**Request:**
```json
{
  "planId": 2,
  "periodo": "MENSUAL"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "suscripcion": {
    "id": 1,
    "usuarioId": 1,
    "planId": 2,
    "fechaInicio": "2024-01-15",
    "fechaFin": "2024-02-15",
    "estado": "ACTIVA",
    "periodo": "MENSUAL"
  }
}
```

**Ejemplo con fetch:**
```javascript
const token = localStorage.getItem('accessToken');
const response = await fetch(`${API_URL}/api/comercial/suscripciones`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    planId: 2,
    periodo: 'MENSUAL'
  })
});

const data = await response.json();
```

## Manejo de Errores

### Códigos HTTP

- `200 OK` - Solicitud exitosa
- `201 Created` - Recurso creado exitosamente
- `400 Bad Request` - Datos inválidos
- `401 Unauthorized` - No autenticado o token inválido
- `404 Not Found` - Recurso no encontrado
- `500 Internal Server Error` - Error del servidor

### Formato de Error

```json
{
  "success": false,
  "message": "Descripción del error"
}
```

### Ejemplo de Manejo de Errores

```javascript
async function makeRequest(url, options = {}) {
  try {
    const response = await fetch(url, options);
    const data = await response.json();
    
    if (!response.ok) {
      if (response.status === 401) {
        // Token expirado, intentar refresh
        await refreshToken();
        // Reintentar request
        return makeRequest(url, options);
      }
      throw new Error(data.message || 'Error en la solicitud');
    }
    
    return data;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

async function refreshToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  const response = await fetch(`${API_URL}/api/auth/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ refreshToken })
  });
  
  const data = await response.json();
  if (data.success) {
    localStorage.setItem('accessToken', data.accessToken);
  } else {
    // Redirigir a login
    window.location.href = '/login';
  }
}
```

## Flujos Comunes

### Flujo de Registro y Suscripción

1. Usuario se registra: `POST /api/auth/register`
2. Usuario hace login: `POST /api/auth/login`
3. Usuario ve planes: `GET /api/comercial/planes`
4. Usuario crea suscripción: `POST /api/comercial/suscripciones`
5. Usuario puede acceder a app.siga.com con el mismo token

### Flujo de Renovación de Token

1. Detectar error 401 en cualquier request
2. Llamar `POST /api/auth/refresh` con refreshToken
3. Actualizar accessToken en localStorage
4. Reintentar request original

## Notas Importantes

- Los tokens JWT expiran después de un tiempo (configurar en backend)
- Guardar tokens de forma segura (localStorage o httpOnly cookies)
- El refreshToken debe guardarse de forma más segura que el accessToken
- Todos los endpoints protegidos requieren el header `Authorization: Bearer {token}`
- El asistente comercial es público, no requiere autenticación
- Los planes son públicos, no requieren autenticación

