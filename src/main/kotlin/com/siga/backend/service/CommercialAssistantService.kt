package com.siga.backend.service

import com.siga.backend.repository.PlanRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class CommercialAssistantService(
    private val geminiService: GeminiService,
    private val planRepository: PlanRepository,
    private val jdbcTemplate: JdbcTemplate
) {
    
    private val systemContext = """
        Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
        Tu función es ayudar a usuarios interesados en conocer los planes y características de SIGA.
        
        Responde sobre:
        - Planes de suscripción disponibles
        - Precios y características de cada plan
        - Trial gratuito
        - Funcionalidades del sistema
        - Proceso de registro y suscripción
        
        Si preguntan sobre inventario o gestión operativa, redirige amablemente a app.siga.com.
        
        Responde siempre en español, de forma amigable y profesional.
        Usa los datos proporcionados en el contexto para dar respuestas precisas.
    """.trimIndent()
    
    fun buildRAGContext(): String {
        val context = mutableListOf<String>()
        
        val planes = planRepository.findByActivoTrueOrderByOrdenAsc()
        if (planes.isNotEmpty()) {
            context.add("=== PLANES DISPONIBLES ===")
            planes.forEach { plan ->
                context.add("""
                    Plan: ${plan.nombre}
                    Precio Mensual: $${plan.precioMensual}
                    ${if (plan.precioAnual != null) "Precio Anual: $${plan.precioAnual}" else ""}
                    Descripción: ${plan.descripcion ?: "Sin descripción"}
                    Límite Bodegas: ${plan.limiteBodegas}
                    Límite Usuarios: ${plan.limiteUsuarios}
                    ${if (plan.limiteProductos != null) "Límite Productos: ${plan.limiteProductos}" else "Productos: Ilimitados"}
                """.trimIndent())
            }
        }
        
        return context.joinToString("\n\n")
    }
    
    fun processMessage(message: String): Result<String> {
        val ragContext = buildRAGContext()
        val fullPrompt = """
            $systemContext
            
            CONTEXTO DE DATOS:
            $ragContext
            
            PREGUNTA DEL USUARIO:
            $message
            
            Responde de forma clara y amigable basándote en el contexto proporcionado.
        """.trimIndent()
        
        return geminiService.generateContent(fullPrompt)
    }
}

