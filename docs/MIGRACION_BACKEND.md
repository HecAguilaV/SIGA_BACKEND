# 🔄 Guía de Migración: De API Key Directa a Backend Proxy

## Estado Actual

✅ **Implementación actual**: API key de Gemini directamente en la app Android  
✅ **Funcional**: El chat funciona correctamente  
⚠️ **Para desarrollo**: Adecuado mientras no tengas backend

## Cuándo Migrar al Backend

Migra al backend cuando:
- ✅ Tengas el backend de SvelteKit implementado
- ✅ Quieras mayor seguridad en producción
- ✅ Necesites controlar uso y límites
- ✅ Quieras agregar autenticación

## Plan de Migración (Paso a Paso)

### Paso 1: Preparar el Backend en SvelteKit

Cuando implementes el backend, crea el endpoint:

```typescript
// src/routes/api/chat/+server.ts
import { json } from '@sveltejs/kit';
import { GoogleGenerativeAI } from '@google/generative-ai';
import type { RequestHandler } from './$types';

const genAI = new GoogleGenerativeAI(import.meta.env.GEMINI_API_KEY);

export const POST: RequestHandler = async ({ request }) => {
    try {
        const { message, history } = await request.json();
        const model = genAI.getGenerativeModel({ model: 'gemini-1.5-flash' });
        
        // Tu contexto SIGA aquí...
        const result = await model.generateContent(prompt);
        
        return json({ 
            response: result.response.text(),
            success: true 
        });
    } catch (error: any) {
        return json({ error: error.message }, { status: 500 });
    }
};
```

### Paso 2: Agregar Dependencia HTTP en Android

En `app/build.gradle.kts`, agrega:

```kotlin
dependencies {
    // ... dependencias existentes
    
    // HTTP Client para llamadas al backend
    implementation("io.ktor:ktor-client-android:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
}
```

### Paso 3: Crear Cliente HTTP

Crea `app/src/main/java/com/example/sigaapp/service/HttpClient.kt`:

```kotlin
package com.example.sigaapp.service

import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

object HttpClient {
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }
}
```

### Paso 4: Actualizar GeminiService

Modifica `GeminiService.kt` para usar el backend:

```kotlin
package com.example.sigaapp.service

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val history: List<ChatHistoryItem>? = null
)

@Serializable
data class ChatHistoryItem(
    val user: String,
    val assistant: String
)

@Serializable
data class ChatResponse(
    val response: String,
    val success: Boolean
)

object GeminiService {
    // Cambiar esta URL cuando tengas el backend desplegado
    private const val BACKEND_URL = "https://siga-prototipo.vercel.app/api/chat"
    
    // Flag para alternar entre backend y API directa
    private const val USE_BACKEND = false // Cambiar a true cuando backend esté listo
    
    suspend fun sendMessage(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        if (!USE_BACKEND) {
            // Usar API directa (implementación actual)
            return@withContext sendMessageDirect(userMessage)
        }
        
        // Usar backend proxy
        try {
            val response = HttpClient.client.post(BACKEND_URL) {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(message = userMessage))
            }
            
            if (response.status.isSuccess()) {
                val result = response.body<ChatResponse>()
                Result.success(result.response)
            } else {
                Result.failure(Exception("Error del servidor: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessageWithHistory(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!USE_BACKEND) {
            return@withContext sendMessageWithHistoryDirect(userMessage, conversationHistory)
        }
        
        val history = conversationHistory.map { (user, assistant) ->
            ChatHistoryItem(user = user, assistant = assistant)
        }
        
        try {
            val response = HttpClient.client.post(BACKEND_URL) {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(message = userMessage, history = history))
            }
            
            if (response.status.isSuccess()) {
                val result = response.body<ChatResponse>()
                Result.success(result.response)
            } else {
                Result.failure(Exception("Error del servidor: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Métodos directos (mantener para compatibilidad)
    private suspend fun sendMessageDirect(userMessage: String): Result<String> {
        // ... código actual de sendMessage ...
    }
    
    private suspend fun sendMessageWithHistoryDirect(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> {
        // ... código actual de sendMessageWithHistory ...
    }
}
```

### Paso 5: Activar el Backend

Cuando el backend esté listo y desplegado:

1. Cambia `USE_BACKEND = true` en `GeminiService.kt`
2. Actualiza `BACKEND_URL` con la URL real de tu backend
3. Compila y prueba

## Ventajas de la Migración

✅ **Seguridad**: API key nunca en la app  
✅ **Control**: Límites de uso y rate limiting  
✅ **Autenticación**: Puedes agregar autenticación de usuarios  
✅ **Cache**: Puedes cachear respuestas comunes  
✅ **Logs**: Logs centralizados de todas las conversaciones  
✅ **Escalabilidad**: Fácil agregar más funcionalidades

## Checklist de Migración

- [ ] Backend SvelteKit implementado y funcionando
- [ ] Endpoint `/api/chat` probado con Postman/curl
- [ ] Backend desplegado en Vercel
- [ ] Dependencias HTTP agregadas en Android
- [ ] `GeminiService.kt` actualizado
- [ ] `USE_BACKEND = true` activado
- [ ] Probado en app Android
- [ ] Remover API key de `local.properties` (opcional, para producción)

## Notas Importantes

- **Por ahora**: La implementación actual funciona perfectamente para desarrollo
- **No hay prisa**: Puedes migrar cuando tengas el backend listo
- **Compatibilidad**: El código está preparado para ambos modos
- **Testing**: Prueba el backend primero antes de activar en la app

---

**Estado**: ✅ Listo para usar API directa ahora  
**Próximo paso**: Implementar backend cuando estés listo

