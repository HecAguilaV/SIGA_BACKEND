package com.siga.backend.services

import com.siga.backend.services.GeminiService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Asistente Comercial - Para el portal comercial (siga.com)
 * 
 * Propósito: Ventas, marketing, soporte pre-venta
 * Acceso: siga_comercial.* (planes, precios, características)
 * Usuarios: Público, clientes (con/sin suscripción)
 */
object CommercialAssistantService {
    
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
    
    /**
     * Construye el contexto RAG para el asistente comercial
     * Recupera información de planes y suscripciones
     */
    suspend fun buildRAGContext(): String {
        val context = mutableListOf<String>()
        
        // Obtener planes disponibles
        val planesList = mutableListOf<String>()
        transaction {
            exec("SELECT id, nombre, precio_mensual, precio_anual, descripcion, caracteristicas " +
                    "FROM siga_comercial.planes " +
                    "WHERE activo = true " +
                    "ORDER BY precio_mensual ASC") { resultSet ->
                while (resultSet.next()) {
                    val nombre = resultSet.getString("nombre")
                    val precioMensual = resultSet.getBigDecimal("precio_mensual")
                    val precioAnual = resultSet.getBigDecimal("precio_anual")
                    val descripcion = resultSet.getString("descripcion")
                    val caracteristicas = resultSet.getString("caracteristicas")
                    
                    planesList.add("""
                        Plan: $nombre
                        Precio mensual: $${precioMensual}
                        Precio anual: $${precioAnual}
                        Descripción: $descripcion
                        Características: $caracteristicas
                    """.trimIndent())
                }
            }
        }
        
        if (planesList.isNotEmpty()) {
            context.add("=== PLANES DISPONIBLES ===\n${planesList.joinToString("\n\n")}")
        } else {
            context.add("=== PLANES DISPONIBLES ===\nNo hay planes disponibles en este momento.")
        }
        
        return context.joinToString("\n\n")
    }
    
    /**
     * Procesa un mensaje del usuario y genera una respuesta
     */
    suspend fun processMessage(userMessage: String): Result<String> {
        return try {
            // Construir contexto RAG
            val ragContext = buildRAGContext()
            
            // Construir prompt completo
            val prompt = """
                $systemContext
                
                === CONTEXTO DE DATOS ===
                $ragContext
                
                === PREGUNTA DEL USUARIO ===
                $userMessage
                
                === RESPUESTA DE SIGA ===
            """.trimIndent()
            
            // Llamar a Gemini
            GeminiService.generateContent(prompt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

