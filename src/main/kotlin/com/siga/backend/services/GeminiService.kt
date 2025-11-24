package com.siga.backend.services

import com.siga.backend.utils.EnvLoader
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Servicio para interactuar con la API de Google Gemini
 */
object GeminiService {
    private val apiKey = EnvLoader.getEnv("GEMINI_API_KEY")
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    /**
     * Genera contenido usando Gemini con un prompt dado
     */
    suspend fun generateContent(prompt: String): Result<String> {
        if (apiKey == null) {
            return Result.failure(Exception("GEMINI_API_KEY no configurada"))
        }
        
        return try {
            val requestBody = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt)
                        )
                    )
                )
            )
            
            val response: GeminiResponse = client.post("$baseUrl?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No se recibió respuesta de Gemini")
            
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cierra el cliente HTTP
     */
    fun close() {
        client.close()
    }
}

// Modelos de datos para la API de Gemini
@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content
)

