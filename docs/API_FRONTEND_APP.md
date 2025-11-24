# API Documentation - SIGA App (Móvil)

Documentación de endpoints para la aplicación móvil Android (siga-app).

## Base URL

- Desarrollo: `http://localhost:8080`
- Producción: `https://siga-backend.railway.app` (actualizar con URL real de Railway)

**Nota**: En todos los ejemplos, usar variable `API_URL` que debe configurarse según el entorno.

## Autenticación

Mismo sistema que la app web. Todos los endpoints requieren:
1. Token JWT en header `Authorization: Bearer {token}`
2. Suscripción activa para endpoints del SaaS

### Endpoints de Autenticación

#### POST /api/auth/login

**Request:**
```kotlin
data class LoginRequest(
    val email: String,
    val password: String
)
```

**Response:**
```kotlin
data class LoginResponse(
    val success: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val userId: Int,
    val email: String,
    val rol: String
)
```

**Ejemplo con Ktor Client:**
```kotlin
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun login(email: String, password: String): LoginResponse {
    val client = HttpClient()
    val apiUrl = "https://siga-backend.railway.app" // Actualizar con URL real
    val response = client.post("$apiUrl/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(email, password))
    }
    
    return response.body()
}
```

#### POST /api/auth/refresh

Renovar token de acceso.

## Productos

### GET /api/saas/productos

Listar productos. Mismo formato que app web.

**Ejemplo con Ktor Client:**
```kotlin
suspend fun getProductos(token: String): ProductosListResponse {
    val client = HttpClient()
    val apiUrl = "https://siga-backend.railway.app" // Actualizar con URL real
    val response = client.get("$apiUrl/api/saas/productos") {
        header("Authorization", "Bearer $token")
    }
    
    if (response.status == HttpStatusCode.PaymentRequired) {
        throw SubscriptionRequiredException()
    }
    
    return response.body()
}
```

### POST /api/saas/productos

Crear producto (solo ADMINISTRADOR).

## Stock

### GET /api/saas/stock

Listar stock. Filtrar por local: `?local_id={id}`

**Ejemplo:**
```kotlin
suspend fun getStock(token: String, localId: Int? = null): StockListResponse {
    val client = HttpClient()
    val apiUrl = "https://siga-backend.railway.app" // Actualizar con URL real
    val url = if (localId != null) {
        "$apiUrl/api/saas/stock?local_id=$localId"
    } else {
        "$apiUrl/api/saas/stock"
    }
    
    val response = client.get(url) {
        header("Authorization", "Bearer $token")
    }
    
    return response.body()
}
```

### POST /api/saas/stock

Actualizar stock.

## Ventas

### GET /api/saas/ventas

Listar ventas del usuario.

### POST /api/saas/ventas

Crear venta. Descuenta stock automáticamente.

**Request:**
```kotlin
data class VentaRequest(
    val localId: Int,
    val detalles: List<DetalleVentaRequest>,
    val observaciones: String? = null
)

data class DetalleVentaRequest(
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: String
)
```

**Ejemplo:**
```kotlin
suspend fun createVenta(
    token: String,
    localId: Int,
    detalles: List<DetalleVentaRequest>
): VentaDetailResponse {
    val client = HttpClient()
    val apiUrl = "https://siga-backend.railway.app" // Actualizar con URL real
    val response = client.post("$apiUrl/api/saas/ventas") {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer $token")
        setBody(VentaRequest(localId, detalles))
    }
    
    if (response.status == HttpStatusCode.BadRequest) {
        val error = response.body<VentaDetailResponse>()
        throw IllegalArgumentException(error.message ?: "Error al crear venta")
    }
    
    return response.body()
}
```

## Asistente Operativo

### POST /api/saas/chat

Chat con asistente operativo.

**Ejemplo:**
```kotlin
suspend fun chatOperativo(token: String, message: String): ChatResponse {
    val client = HttpClient()
    val apiUrl = "https://siga-backend.railway.app" // Actualizar con URL real
    val response = client.post("$apiUrl/api/saas/chat") {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer $token")
        setBody(ChatRequest(message))
    }
    
    return response.body()
}
```

## Cliente HTTP Reutilizable

### Implementación con Ktor Client

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultrequest.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(private val tokenManager: TokenManager) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        defaultRequest {
            url("https://siga-backend.railway.app") // Actualizar con URL real
            header("Content-Type", "application/json")
        }
        
        // Interceptor para agregar token y manejar refresh
        engine {
            addInterceptor { request ->
                val token = tokenManager.getAccessToken()
                request.header("Authorization", "Bearer $token")
                
                val response = proceed(request)
                
                // Si token expirado, refresh y reintentar
                if (response.status == HttpStatusCode.Unauthorized) {
                    tokenManager.refreshToken()
                    request.header("Authorization", "Bearer ${tokenManager.getAccessToken()}")
                    proceed(request)
                } else {
                    response
                }
            }
        }
    }
    
    suspend fun getProductos(): ProductosListResponse {
        val response = client.get("${client.config.defaultRequest.url}/api/saas/productos")
        
        if (response.status == HttpStatusCode.PaymentRequired) {
            throw SubscriptionRequiredException()
        }
        
        return response.body()
    }
    
    suspend fun createVenta(venta: VentaRequest): VentaDetailResponse {
        val response = client.post("/api/saas/ventas") {
            setBody(venta)
        }
        
        return response.body()
    }
    
    suspend fun chat(message: String): ChatResponse {
        val response = client.post("/api/saas/chat") {
            setBody(ChatRequest(message))
        }
        
        return response.body()
    }
}

// Token Manager
class TokenManager(private val prefs: SharedPreferences) {
    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }
    
    suspend fun refreshToken() {
        val refreshToken = getRefreshToken() ?: throw NotAuthenticatedException()
        // Llamar a /api/auth/refresh
        // Guardar nuevo accessToken
    }
}
```

## Manejo de Errores

### Excepciones Personalizadas

```kotlin
class SubscriptionRequiredException : Exception("Se requiere suscripción activa")
class NotAuthenticatedException : Exception("No autenticado")
class InsufficientStockException(message: String) : Exception(message)
```

### Manejo de Respuestas

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: SubscriptionRequiredException) {
        // Mostrar pantalla de suscripción
        Result.failure(e)
    } catch (e: NotAuthenticatedException) {
        // Redirigir a login
        Result.failure(e)
    } catch (e: Exception) {
        // Error genérico
        Result.failure(e)
    }
}

// Uso
val result = safeApiCall { apiClient.getProductos() }
result.onSuccess { productos ->
    // Mostrar productos
}.onFailure { error ->
    // Manejar error
}
```

## Flujos Comunes

### Flujo de Venta en Móvil

1. Usuario escanea código de barras o selecciona producto
2. Agregar a carrito local
3. Al finalizar, crear venta: `POST /api/saas/ventas`
4. Mostrar confirmación
5. Actualizar lista de productos/stock

### Flujo de Consulta con Asistente

1. Usuario habla o escribe pregunta
2. Llamar: `POST /api/saas/chat`
3. Mostrar respuesta en UI
4. Opcional: mostrar datos relacionados

## Notas Importantes

- Usar Ktor Client para Android
- Guardar tokens en SharedPreferences o DataStore
- Implementar refresh automático de tokens
- Manejar offline: cachear datos localmente
- Todos los endpoints del SaaS requieren suscripción activa
- OPERADOR solo ve datos de sus locales asignados
- Las ventas descuentan stock automáticamente

## Dependencias Recomendadas

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.ktor:ktor-client-android:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
```

