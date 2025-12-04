package com.siga.backend.service

import com.fasterxml.jackson.annotation.JsonProperty
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
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private val webClient = WebClient.builder().build()
    
    fun generateContent(prompt: String): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(Exception("GEMINI_API_KEY no configurada"))
        }
        
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            val response = runBlocking {
                webClient.post()
                    .uri("$baseUrl?key=$apiKey")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono<GeminiResponse>()
                    .awaitSingle()
            }
            
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("Respuesta vacía de Gemini"))
            
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

