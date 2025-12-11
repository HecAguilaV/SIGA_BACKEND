package com.siga.backend.service

import com.siga.backend.repository.PlanRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class CommercialAssistantService(
    private val geminiService: GeminiService,
    private val planRepository: PlanRepository,
    private val jdbcTemplate: JdbcTemplate
) {
    private val logger = LoggerFactory.getLogger(CommercialAssistantService::class.java)
    
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
        
        try {
            logger.debug("Construyendo contexto RAG para chat comercial")
            val planes = planRepository.findByActivoTrueOrderByOrdenAsc()
            logger.debug("Planes encontrados: ${planes.size}")
            
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
            } else {
                logger.warn("No se encontraron planes activos en la base de datos")
                context.add("=== PLANES DISPONIBLES ===")
                context.add("No hay planes disponibles en este momento.")
            }
        } catch (e: Exception) {
            logger.error("Error al construir contexto RAG para chat comercial", e)
            context.add("=== PLANES DISPONIBLES ===")
            context.add("Error al cargar información de planes.")
        }
        
        val contextString = context.joinToString("\n\n")
        logger.debug("Contexto RAG construido: ${contextString.length} caracteres")
        return contextString
    }
    
    fun processMessage(message: String): Result<String> {
        try {
            logger.debug("Procesando mensaje comercial: ${message.take(50)}...")
            val ragContext = buildRAGContext()
            logger.debug("Contexto RAG construido: ${ragContext.length} caracteres")
            
            val fullPrompt = """
            $systemContext
            
            CONTEXTO DE DATOS:
            $ragContext
            
            PREGUNTA DEL USUARIO:
            $message
            
            Responde de forma clara y amigable basándote en el contexto proporcionado.
        """.trimIndent()
            
            logger.debug("Enviando prompt a Gemini (${fullPrompt.length} caracteres)")
            val result = geminiService.generateContent(fullPrompt)
            
            result.onSuccess {
                logger.debug("Respuesta exitosa de Gemini (${it.length} caracteres)")
            }.onFailure { error ->
                logger.error("Error al generar contenido con Gemini", error)
            }
            
            return result
        } catch (e: Exception) {
            logger.error("Excepción no controlada en processMessage comercial", e)
            return Result.failure(e)
        }
    }
}

