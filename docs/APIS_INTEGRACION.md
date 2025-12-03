# APIs e Integración - SIGA Backend

## Introducción

Este documento describe cómo integrar aplicaciones frontend y móviles con el backend de SIGA. Proporciona ejemplos prácticos y mejores prácticas para el consumo de la API.

## Configuración Base

### URL Base

**Producción**: `https://siga-backend-production.up.railway.app`  
**Desarrollo**: `http://localhost:8080`

### Headers Comunes

Todas las solicitudes deben incluir:

```javascript
{
  "Content-Type": "application/json",
  "Authorization": "Bearer <access_token>" // Para endpoints protegidos
}
```

## Flujo de Autenticación

### 1. Registro de Usuario

```javascript
const register = async (userData) => {
  const response = await fetch('https://siga-backend-production.up.railway.app/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: userData.email,
      password: userData.password,
      nombre: userData.nombre,
      apellido: userData.apellido,
      rol: 'OPERADOR' // o 'ADMINISTRADOR', 'CAJERO'
    })
  });
  
  const data = await response.json();
  
  if (data.success) {
    // Guardar tokens
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  
  throw new Error(data.message);
};
```

### 2. Login

```javascript
const login = async (email, password) => {
  const response = await fetch('https://siga-backend-production.up.railway.app/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email, password })
  });
  
  const data = await response.json();
  
  if (data.success) {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  
  throw new Error(data.message || 'Credenciales inválidas');
};
```

### 3. Refresh Token

```javascript
const refreshToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch('https://siga-backend-production.up.railway.app/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ refreshToken })
  });
  
  const data = await response.json();
  
  if (data.success) {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  
  // Token inválido, redirigir a login
  localStorage.clear();
  window.location.href = '/login';
};
```

## Cliente API Reutilizable

### JavaScript/TypeScript

```typescript
class SIGAApiClient {
  private baseURL: string;
  
  constructor(baseURL: string = 'https://siga-backend-production.up.railway.app') {
    this.baseURL = baseURL;
  }
  
  private async request(endpoint: string, options: RequestInit = {}) {
    const token = localStorage.getItem('accessToken');
    
    const headers = {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers
    };
    
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      ...options,
      headers
    });
    
    // Manejar token expirado
    if (response.status === 401) {
      try {
        await this.refreshToken();
        // Reintentar con nuevo token
        return this.request(endpoint, options);
      } catch (error) {
        // Refresh falló, redirigir a login
        window.location.href = '/login';
        throw error;
      }
    }
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Error en la solicitud');
    }
    
    return response.json();
  }
  
  // Autenticación
  async register(userData: RegisterData) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  }
  
  async login(email: string, password: string) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
  }
  
  async refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    const data = await this.request('/api/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken })
    });
    
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  
  // Productos
  async getProductos() {
    return this.request('/api/saas/productos');
  }
  
  async getProducto(id: number) {
    return this.request(`/api/saas/productos/${id}`);
  }
  
  async createProducto(producto: ProductoData) {
    return this.request('/api/saas/productos', {
      method: 'POST',
      body: JSON.stringify(producto)
    });
  }
  
  async updateProducto(id: number, producto: ProductoData) {
    return this.request(`/api/saas/productos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(producto)
    });
  }
  
  async deleteProducto(id: number) {
    return this.request(`/api/saas/productos/${id}`, {
      method: 'DELETE'
    });
  }
  
  // Stock
  async getStock(localId?: number) {
    const query = localId ? `?localId=${localId}` : '';
    return this.request(`/api/saas/stock${query}`);
  }
  
  async updateStock(stock: StockData) {
    return this.request('/api/saas/stock', {
      method: 'POST',
      body: JSON.stringify(stock)
    });
  }
  
  // Ventas
  async getVentas() {
    return this.request('/api/saas/ventas');
  }
  
  async createVenta(venta: VentaData) {
    return this.request('/api/saas/ventas', {
      method: 'POST',
      body: JSON.stringify(venta)
    });
  }
  
  // Planes
  async getPlanes() {
    return this.request('/api/comercial/planes');
  }
  
  async getPlan(id: number) {
    return this.request(`/api/comercial/planes/${id}`);
  }
  
  // Suscripciones
  async getSuscripciones() {
    return this.request('/api/comercial/suscripciones');
  }
  
  async createSuscripcion(suscripcion: SuscripcionData) {
    return this.request('/api/comercial/suscripciones', {
      method: 'POST',
      body: JSON.stringify(suscripcion)
    });
  }
  
  // Chat
  async chatComercial(message: string) {
    return this.request('/api/comercial/chat', {
      method: 'POST',
      body: JSON.stringify({ message })
    });
  }
  
  async chatOperativo(message: string) {
    return this.request('/api/saas/chat', {
      method: 'POST',
      body: JSON.stringify({ message })
    });
  }
}

// Uso
const api = new SIGAApiClient();

// Ejemplo de uso
const productos = await api.getProductos();
console.log(productos);
```

### Kotlin (Android)

```kotlin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SIGAApiClient(private val baseURL: String = "https://siga-backend-production.up.railway.app") {
    
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()
    
    private fun getToken(): String? {
        // Obtener token del SharedPreferences o similar
        return sharedPreferences.getString("access_token", null)
    }
    
    private fun request(
        endpoint: String,
        method: String = "GET",
        body: JSONObject? = null
    ): JSONObject {
        val url = "$baseURL$endpoint"
        val requestBuilder = Request.Builder().url(url)
        
        // Agregar token si existe
        getToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        requestBuilder.addHeader("Content-Type", "application/json")
        
        when (method) {
            "POST", "PUT" -> {
                val requestBody = body?.toString()?.toRequestBody(jsonMediaType)
                requestBuilder.method(method, requestBody)
            }
            "DELETE" -> {
                requestBuilder.delete()
            }
        }
        
        val request = requestBuilder.build()
        val response = client.newCall(request).execute()
        
        if (response.code == 401) {
            // Intentar refresh token
            refreshToken()
            // Reintentar
            return request(endpoint, method, body)
        }
        
        if (!response.isSuccessful) {
            throw Exception("Error: ${response.code}")
        }
        
        val responseBody = response.body?.string()
        return JSONObject(responseBody ?: "{}")
    }
    
    fun login(email: String, password: String): JSONObject {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val response = request("/api/auth/login", "POST", body)
        
        // Guardar tokens
        sharedPreferences.edit().apply {
            putString("access_token", response.getString("accessToken"))
            putString("refresh_token", response.getString("refreshToken"))
            apply()
        }
        
        return response
    }
    
    fun getProductos(): JSONObject {
        return request("/api/saas/productos")
    }
    
    fun createProducto(producto: Map<String, Any>): JSONObject {
        val body = JSONObject(producto)
        return request("/api/saas/productos", "POST", body)
    }
    
    // ... más métodos
}
```

## Manejo de Errores

### Códigos de Estado Comunes

```javascript
const handleApiError = (error, response) => {
  switch (response?.status) {
    case 400:
      return 'Solicitud inválida. Verifica los datos enviados.';
    case 401:
      return 'No autenticado. Por favor inicia sesión.';
    case 403:
      return 'No tienes permisos para realizar esta acción.';
    case 404:
      return 'Recurso no encontrado.';
    case 409:
      return 'Conflicto. El recurso ya existe.';
    case 402:
      return 'Se requiere una suscripción activa.';
    case 500:
      return 'Error del servidor. Intenta más tarde.';
    default:
      return error.message || 'Error desconocido';
  }
};
```

## Ejemplos de Integración por Frontend

### Web Comercial (React)

```javascript
// hooks/useAuth.js
import { useState, useEffect } from 'react';
import { api } from '../services/api';

export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      // Verificar token y obtener usuario
      // Implementar lógica
    }
    setLoading(false);
  }, []);
  
  const login = async (email, password) => {
    const data = await api.login(email, password);
    setUser(data.user);
    return data;
  };
  
  return { user, login, loading };
};
```

### App Web Operativa (SvelteKit)

```javascript
// lib/api.js
import { browser } from '$app/environment';

export const api = {
  async request(endpoint, options = {}) {
    const token = browser ? localStorage.getItem('accessToken') : null;
    
    const response = await fetch(`https://siga-backend-production.up.railway.app${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
      }
    });
    
    return response.json();
  },
  
  async getProductos() {
    return this.request('/api/saas/productos');
  }
};
```

### App Android (Kotlin)

```kotlin
// ViewModel
class ProductosViewModel : ViewModel() {
    private val apiClient = SIGAApiClient()
    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos
    
    fun loadProductos() {
        viewModelScope.launch {
            try {
                val response = apiClient.getProductos()
                val productosList = parseProductos(response)
                _productos.postValue(productosList)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
```

## Mejores Prácticas

### 1. Manejo de Tokens
- Almacenar tokens de forma segura (localStorage para web, SecureStorage para móvil)
- Implementar refresh automático antes de la expiración
- Limpiar tokens al cerrar sesión

### 2. Manejo de Errores
- Implementar manejo centralizado de errores
- Mostrar mensajes de error amigables al usuario
- Registrar errores para debugging

### 3. Optimización
- Implementar caché para datos que no cambian frecuentemente
- Usar paginación para listas grandes
- Implementar debounce para búsquedas

### 4. Seguridad
- Nunca exponer tokens en logs o consola
- Validar datos antes de enviar
- Usar HTTPS siempre en producción

## Testing de Integración

### Ejemplo con Jest

```javascript
describe('SIGA API Integration', () => {
  let api;
  
  beforeAll(() => {
    api = new SIGAApiClient('http://localhost:8080');
  });
  
  test('should register and login user', async () => {
    const registerData = {
      email: 'test@example.com',
      password: 'password123',
      nombre: 'Test',
      rol: 'OPERADOR'
    };
    
    const registerResponse = await api.register(registerData);
    expect(registerResponse.success).toBe(true);
    
    const loginResponse = await api.login(registerData.email, registerData.password);
    expect(loginResponse.success).toBe(true);
    expect(loginResponse.accessToken).toBeDefined();
  });
  
  test('should get productos after authentication', async () => {
    await api.login('test@example.com', 'password123');
    const productos = await api.getProductos();
    expect(productos.success).toBe(true);
    expect(Array.isArray(productos.productos)).toBe(true);
  });
});
```

## Conclusión

Esta guía proporciona los fundamentos para integrar cualquier frontend con el backend de SIGA. Para más detalles, consultar la documentación de Swagger UI o el documento de API_DOCUMENTACION.md.

