# 🚀 Guía para Implementar Backend Proxy - SIGA

## Opción Recomendada: SvelteKit API Endpoint

Ya tienes SvelteKit en tu proyecto prototipo, así que puedes agregar un endpoint API allí.

### Estructura del Endpoint

En tu proyecto SvelteKit (`SIGA_PROTOTIPO`), crea:

```
src/routes/api/chat/+server.ts
```

### Código del Endpoint

```typescript
// src/routes/api/chat/+server.ts
import { json } from '@sveltejs/kit';
import { GoogleGenerativeAI } from '@google/generative-ai';
import type { RequestHandler } from './$types';

const genAI = new GoogleGenerativeAI(import.meta.env.GEMINI_API_KEY);

const sigaContext = `
Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
[Tu contexto SIGA aquí]
`.trim();

export const POST: RequestHandler = async ({ request }) => {
    try {
        const { message, history } = await request.json();

        if (!message) {
            return json({ error: 'Mensaje requerido' }, { status: 400 });
        }

        const model = genAI.getGenerativeModel({ model: 'gemini-1.5-flash' });

        // Construir prompt con historial si existe
        let prompt = sigaContext;
        if (history && history.length > 0) {
            const historyText = history
                .map((h: { user: string; assistant: string }) => 
                    `Usuario: ${h.user}\nSIGA: ${h.assistant}`
                )
                .join('\n\n');
            prompt += `\n\nHistorial:\n${historyText}\n\n`;
        }
        prompt += `\nUsuario: ${message}\n\nSIGA:`;

        const result = await model.generateContent(prompt);
        const response = result.response;
        const text = response.text();

        return json({ 
            response: text,
            success: true 
        });
    } catch (error: any) {
        console.error('Error en Gemini API:', error);
        return json(
            { 
                error: 'Error al procesar el mensaje',
                details: error.message 
            },
            { status: 500 }
        );
    }
};
```

### Variables de Entorno

En tu proyecto SvelteKit, agrega en `.env`:

```env
GEMINI_API_KEY=tu_api_key_gemini_aqui
```

Y en Vercel, agrega la variable de entorno en el dashboard.

### Modificar la App Android

Actualiza `GeminiService.kt` para usar tu backend:

```kotlin
object GeminiService {
    // Cambiar a la URL de tu backend
    private const val BACKEND_URL = "https://tu-proyecto.vercel.app/api/chat"
    
    suspend fun sendMessage(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post(BACKEND_URL) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("message" to userMessage))
            }
            
            if (response.status.isSuccess()) {
                val result = response.body<ChatResponse>()
                Result.success(result.response)
            } else {
                Result.failure(Exception("Error del servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

Necesitarás agregar la dependencia de HTTP client (Ktor o Retrofit).

---

## Opción 2: Backend Node.js/Express Separado

Si prefieres un proyecto completamente separado:

### Estructura

```
siga-backend/
├── src/
│   ├── index.js
│   └── routes/
│       └── chat.js
├── package.json
└── .env
```

### Código Básico

```javascript
// src/index.js
const express = require('express');
const cors = require('cors');
const { GoogleGenerativeAI } = require('@google/generative-ai');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

app.post('/api/chat', async (req, res) => {
    try {
        const { message, history } = req.body;
        
        const model = genAI.getGenerativeModel({ model: 'gemini-1.5-flash' });
        
        // Construir prompt...
        const result = await model.generateContent(prompt);
        const response = result.response.text();
        
        res.json({ response, success: true });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Servidor corriendo en puerto ${PORT}`);
});
```

### Despliegue

- **Vercel**: Conecta el repo y despliega
- **Railway**: Fácil despliegue de Node.js
- **Render**: Gratis para empezar
- **Heroku**: Opción tradicional

---

## Opción 3: Firebase Functions

Si prefieres serverless puro:

```javascript
// functions/index.js
const functions = require('firebase-functions');
const { GoogleGenerativeAI } = require('@google/generative-ai');

exports.chat = functions.https.onRequest(async (req, res) => {
    // Código similar al anterior
});
```

---

## Comparación de Opciones

| Opción | Complejidad | Costo | Recomendación |
|--------|-------------|-------|---------------|
| **SvelteKit** | ⭐ Baja | Gratis (Vercel) | ✅ Mejor opción |
| **Node.js/Express** | ⭐⭐ Media | Gratis/Bajo | ✅ Buena opción |
| **Firebase Functions** | ⭐⭐ Media | Gratis/Bajo | ✅ Alternativa |

---

## Pasos Siguientes

1. **Elegir opción** (recomiendo SvelteKit)
2. **Crear endpoint** con el código de arriba
3. **Configurar variables de entorno** en Vercel
4. **Actualizar Android app** para usar el backend
5. **Probar** la integración

---

## Ventajas del Backend Proxy

✅ API key nunca está en la app Android  
✅ Control de uso y límites  
✅ Puedes agregar autenticación  
✅ Puedes cachear respuestas  
✅ Logs centralizados  
✅ Más seguro para producción

