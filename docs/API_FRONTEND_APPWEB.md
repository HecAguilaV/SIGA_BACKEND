# API Documentation - SIGA App Web

Documentación de endpoints para la aplicación web SaaS (app.siga.com).

## Base URL

- Desarrollo: `http://localhost:8080`
- Producción: `https://siga-backend-production.up.railway.app`

## Frontend URL

- Desarrollo: `http://localhost:5173` o `http://localhost:3000`
- Producción: `https://siga-appweb.vercel.app`

## Configuración de API_URL

En el código frontend, configurar la variable de entorno `API_URL`:

```javascript
// Usar variable de entorno o definir según el entorno
const API_URL = process.env.API_URL || 'https://siga-backend-production.up.railway.app';
```

**Nota**: En producción, configurar `API_URL` como variable de entorno en Vercel.

## Autenticación

Todos los endpoints (excepto los de autenticación) requieren:
1. Token JWT válido en el header `Authorization: Bearer {token}`
2. Suscripción activa en `siga_comercial.SUSCRIPCIONES`

### Endpoints de Autenticación

#### POST /api/auth/login

**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "usuario@example.com",
  "rol": "ADMINISTRADOR"
}
```

**Ejemplo:**
```javascript
const login = async (email, password) => {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  const data = await response.json();
  if (data.success) {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  throw new Error(data.message);
};
```

## Productos

### GET /api/saas/productos

Listar todos los productos activos.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "success": true,
  "productos": [
    {
      "id": 1,
      "nombre": "Producto 1",
      "descripcion": "Descripción",
      "categoriaId": 1,
      "codigoBarras": "123456789",
      "precioUnitario": "1000.50",
      "activo": true,
      "fechaCreacion": "2024-01-01T00:00:00Z",
      "fechaActualizacion": "2024-01-01T00:00:00Z"
    }
  ],
  "total": 10
}
```

**Ejemplo:**
```javascript
const getProductos = async () => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`${API_URL}/api/saas/productos`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  if (response.status === 402) {
    throw new Error('Se requiere suscripción activa');
  }
  
  return response.json();
};
```

### GET /api/saas/productos/{id}

Obtener producto por ID.

### POST /api/saas/productos

Crear producto (solo ADMINISTRADOR).

**Request:**
```json
{
  "nombre": "Nuevo Producto",
  "descripcion": "Descripción del producto",
  "categoriaId": 1,
  "codigoBarras": "123456789",
  "precioUnitario": "1500.00"
}
```

**Ejemplo:**
```javascript
const createProducto = async (producto) => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`${API_URL}/api/saas/productos`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(producto)
  });
  
  if (response.status === 403) {
    throw new Error('Solo administradores pueden crear productos');
  }
  
  return response.json();
};
```

### PUT /api/saas/productos/{id}

Actualizar producto (solo ADMINISTRADOR).

### DELETE /api/saas/productos/{id}

Eliminar producto (soft delete, solo ADMINISTRADOR).

## Stock

### GET /api/saas/stock

Listar stock. Opcional: `?local_id={id}` para filtrar por local.

**Ejemplo:**
```javascript
const getStock = async (localId = null) => {
  const token = localStorage.getItem('accessToken');
  const url = localId
    ? `${API_URL}/api/saas/stock?local_id=${localId}`
    : `${API_URL}/api/saas/stock`;
    
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return response.json();
};
```

### GET /api/saas/stock/{producto_id}/{local_id}

Obtener stock específico de un producto en un local.

### POST /api/saas/stock

Agregar o actualizar stock.

**Request:**
```json
{
  "productoId": 1,
  "localId": 1,
  "cantidad": 50,
  "cantidadMinima": 10
}
```

## Ventas

### GET /api/saas/ventas

Listar ventas. OPERADOR ve solo sus ventas, ADMINISTRADOR ve todas.

**Ejemplo:**
```javascript
const getVentas = async () => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`${API_URL}/api/saas/ventas`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return response.json();
};
```

### GET /api/saas/ventas/{id}

Obtener venta con detalles.

### POST /api/saas/ventas

Crear venta. Descuenta stock automáticamente.

**Request:**
```json
{
  "localId": 1,
  "detalles": [
    {
      "productoId": 1,
      "cantidad": 2,
      "precioUnitario": "1000.00"
    },
    {
      "productoId": 2,
      "cantidad": 1,
      "precioUnitario": "2000.00"
    }
  ],
  "observaciones": "Venta al contado"
}
```

**Response:**
```json
{
  "success": true,
  "venta": {
    "id": 1,
    "localId": 1,
    "usuarioId": 1,
    "fecha": "2024-01-15T10:30:00Z",
    "total": "4000.00",
    "estado": "COMPLETADA",
    "observaciones": "Venta al contado",
    "detalles": [
      {
        "id": 1,
        "productoId": 1,
        "cantidad": 2,
        "precioUnitario": "1000.00",
        "subtotal": "2000.00"
      }
    ]
  }
}
```

**Ejemplo:**
```javascript
const createVenta = async (ventaData) => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`${API_URL}/api/saas/ventas`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(ventaData)
  });
  
  if (response.status === 400) {
    const error = await response.json();
    throw new Error(error.message); // Ej: "Stock insuficiente para: Producto X"
  }
  
  return response.json();
};
```

## Asistente Operativo

### POST /api/saas/chat

Chat con el asistente operativo (requiere autenticación y suscripción).

**Request:**
```json
{
  "message": "¿Cuánto stock hay de Café Frío en ITR?"
}
```

**Response:**
```json
{
  "response": "Hay 26 unidades de Café Frío Listo 350ml en ITR",
  "success": true
}
```

**Ejemplo:**
```javascript
const chatOperativo = async (message) => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`${API_URL}/api/saas/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ message })
  });
  
  if (response.status === 402) {
    throw new Error('Se requiere suscripción activa');
  }
  
  return response.json();
};
```

## Manejo de Errores

### Códigos HTTP Específicos

- `200 OK` - Solicitud exitosa
- `201 Created` - Recurso creado
- `400 Bad Request` - Datos inválidos (ej: stock insuficiente)
- `401 Unauthorized` - No autenticado
- `402 Payment Required` - **Suscripción requerida** (endpoints del SaaS)
- `403 Forbidden` - Sin permisos (ej: OPERADOR intenta crear producto)
- `404 Not Found` - Recurso no encontrado
- `500 Internal Server Error` - Error del servidor

### Interceptor para Manejo de Tokens

```javascript
// Interceptor para agregar token y manejar refresh
const apiClient = {
  async request(url, options = {}) {
    const token = localStorage.getItem('accessToken');
    
    const config = {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers
      }
    };
    
    let response = await fetch(`${API_URL}${url}`, config);
    
    // Si token expirado, intentar refresh
    if (response.status === 401) {
      const refreshed = await this.refreshToken();
      if (refreshed) {
        config.headers['Authorization'] = `Bearer ${localStorage.getItem('accessToken')}`;
        response = await fetch(`${API_URL}${url}`, config);
      }
    }
    
    // Si requiere suscripción
    if (response.status === 402) {
      throw new Error('Se requiere una suscripción activa');
    }
    
    return response.json();
  },
  
  async refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await fetch(`${API_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    const data = await response.json();
    if (data.success) {
      localStorage.setItem('accessToken', data.accessToken);
      return true;
    }
    
    // Redirigir a login
    window.location.href = '/login';
    return false;
  }
};

// Uso
const productos = await apiClient.request('/api/saas/productos');
```

## Flujos Comunes

### Flujo de Venta Completo

1. Usuario selecciona productos y cantidades
2. Validar stock antes de crear venta (opcional, el backend también valida)
3. Crear venta: `POST /api/saas/ventas`
4. Si éxito: stock se descuenta automáticamente
5. Mostrar confirmación y actualizar lista de productos/stock

### Flujo de Consulta con Asistente

1. Usuario escribe pregunta: "¿Cuánto stock hay de X?"
2. Llamar: `POST /api/saas/chat` con el mensaje
3. Mostrar respuesta del asistente
4. Opcional: actualizar UI con datos mostrados por el asistente

## Notas Importantes

- Todos los endpoints del SaaS requieren suscripción activa
- OPERADOR solo ve datos de sus locales asignados
- ADMINISTRADOR ve todos los datos
- Las ventas descuentan stock automáticamente
- El asistente operativo tiene acceso a datos según el rol del usuario
- Guardar tokens de forma segura
- Implementar refresh automático de tokens

