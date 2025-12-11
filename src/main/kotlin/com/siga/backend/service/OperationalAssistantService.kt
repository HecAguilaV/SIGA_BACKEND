package com.siga.backend.service

import com.siga.backend.repository.UsuarioSaasRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class OperationalAssistantService(
    private val geminiService: GeminiService,
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val jdbcTemplate: JdbcTemplate
) {
    
    private val systemContext = """
        Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
        Ayudas a usuarios a gestionar su inventario, consultar stock, ver ventas, etc.
        
        Responde de forma amigable y profesional en español.
        Usa los datos proporcionados en el contexto para dar respuestas precisas.
        
        Si no tienes información suficiente en el contexto, indica que necesitas más datos.
        Sé conciso pero completo en tus respuestas.
    """.trimIndent()
    
    fun buildRAGContext(userId: Int, userRol: String, query: String): String {
        val context = mutableListOf<String>()
        
        val user = usuarioSaasRepository.findById(userId).orElse(null)
            ?: return "Usuario no encontrado"
        
        context.add("=== INFORMACIÓN DEL USUARIO ===\nUsuario: ${user.nombre} (${user.email})\nRol: $userRol")
        
        // Si es ADMINISTRADOR, puede ver todo el inventario
        if (userRol == "ADMINISTRADOR") {
            val productos = jdbcTemplate.queryForList(
                "SELECT id, nombre, precio_unitario FROM siga_saas.PRODUCTOS WHERE activo = true LIMIT 50"
            )
            if (productos.isNotEmpty()) {
                context.add("\n=== PRODUCTOS ===")
                productos.forEach { producto ->
                    context.add("${producto["nombre"]} (ID: ${producto["id"]}) - Precio: $${producto["precio_unitario"]}")
                }
            }
            
            val stock = jdbcTemplate.queryForList(
                "SELECT p.nombre, s.cantidad, l.nombre as local FROM siga_saas.STOCK s " +
                "JOIN siga_saas.PRODUCTOS p ON s.producto_id = p.id " +
                "JOIN siga_saas.LOCALES l ON s.local_id = l.id " +
                "LIMIT 50"
            )
            if (stock.isNotEmpty()) {
                context.add("\n=== STOCK ===")
                stock.forEach { item ->
                    context.add("${item["nombre"]} en ${item["local"]}: ${item["cantidad"]} unidades")
                }
            }
        }
        
        return context.joinToString("\n\n")
    }
    
    fun processMessage(message: String, userId: Int, userRol: String?): Result<String> {
        val logger = org.slf4j.LoggerFactory.getLogger(OperationalAssistantService::class.java)
        
        try {
            logger.debug("Procesando mensaje para usuario $userId con rol ${userRol ?: "OPERADOR"}")
            val ragContext = buildRAGContext(userId, userRol ?: "OPERADOR", message)
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
            logger.error("Excepción no controlada en processMessage", e)
            return Result.failure(e)
        }
    }
}

