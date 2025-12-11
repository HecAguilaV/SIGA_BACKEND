package com.siga.backend.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.siga.backend.exception.GeminiApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)

@Service
class GeminiService(
    @Value("\${gemini.api-key}") private val apiKey: String
) {
    private val logger = LoggerFactory.getLogger(GeminiService::class.java)
    // Usar gemini-1.5-flash que es más estable y ampliamente disponible
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private val webClient = WebClient.builder().build()
    
    fun generateContent(prompt: String): Result<String> {
        if (apiKey.isBlank()) {
            logger.error("GEMINI_API_KEY no configurada o vacía")
            return Result.failure(GeminiApiException("API key de Gemini no configurada"))
        }
        
        logger.debug("API Key configurada: ${apiKey.take(10)}... (longitud: ${apiKey.length})")
        
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            logger.debug("Enviando solicitud a Gemini API: $baseUrl")
            logger.debug("Prompt length: ${prompt.length} caracteres")
            
            val response = runBlocking {
                webClient.post()
                    .uri("$baseUrl?key=$apiKey")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus({ status -> status.isError }) { response ->
                        logger.error("Error HTTP de Gemini: ${response.statusCode()}")
                        response.bodyToMono<String>()
                            .doOnNext { body -> logger.error("Cuerpo de error: $body") }
                            .then()
                    }
                    .bodyToMono<GeminiResponse>()
                    .awaitSingle()
            }
            
            logger.debug("Respuesta recibida de Gemini, procesando...")
            
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: run {
                    logger.warn("Respuesta vacía de Gemini. Candidates: ${response.candidates.size}")
                    return Result.failure(GeminiApiException("Respuesta vacía de Gemini"))
                }
            
            logger.debug("Respuesta exitosa de Gemini API (${text.length} caracteres)")
            Result.success(text)
        } catch (e: Exception) {
            logger.error("Error al llamar a Gemini API: ${e.javaClass.simpleName}", e)
            logger.error("Mensaje de error: ${e.message}")
            if (e.cause != null) {
                logger.error("Causa: ${e.cause?.javaClass?.simpleName} - ${e.cause?.message}")
            }
            // Crear excepción personalizada con el error original, pero el mensaje será sanitizado
            Result.failure(GeminiApiException("Error al comunicarse con Gemini API: ${e.message}", e))
        }
    }
}

