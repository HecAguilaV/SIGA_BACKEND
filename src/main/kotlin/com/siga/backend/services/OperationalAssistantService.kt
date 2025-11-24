package com.siga.backend.services

import com.siga.backend.models.UsuarioSaasTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Asistente Operativo - Para el sistema SaaS (app.siga.com)
 * 
 * Propósito: Operaciones del negocio (inventario, stock, ventas)
 * Acceso: siga_saas.* (según rol del usuario)
 * Requisitos: Autenticación JWT + Suscripción activa
 */
object OperationalAssistantService {
    
    private val systemContext = """
        Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
        Ayudas a usuarios a gestionar su inventario, consultar stock, ver ventas, etc.
        
        Responde de forma amigable y profesional en español.
        Usa los datos proporcionados en el contexto para dar respuestas precisas.
        
        Si no tienes información suficiente en el contexto, indica que necesitas más datos.
        Sé conciso pero completo en tus respuestas.
    """.trimIndent()
    
    /**
     * Construye el contexto RAG según el rol del usuario
     */
    suspend fun buildRAGContext(userId: Int, userRol: String, query: String): String {
        val context = mutableListOf<String>()
        
        // Obtener información del usuario
        val user = transaction {
            UsuarioSaasTable.select {
                UsuarioSaasTable.id eq userId
            }.firstOrNull()
        }
        
        if (user == null) {
            return "Usuario no encontrado"
        }
        
        val userEmail = user[UsuarioSaasTable.email]
        val userName = user[UsuarioSaasTable.nombre]
        
        context.add("=== INFORMACIÓN DEL USUARIO ===\nUsuario: $userName ($userEmail)\nRol: $userRol")
        
        // Si es ADMINISTRADOR, puede ver todo el inventario
        if (userRol == "ADMINISTRADOR") {
            // Obtener productos y stock
            val productos = mutableListOf<String>()
            transaction {
                exec("""
                    SELECT p.id, p.nombre, p.sku, p.precio_venta, 
                           COALESCE(SUM(s.cantidad), 0) as stock_total
                    FROM siga_saas.productos p
                    LEFT JOIN siga_saas.stock s ON p.id = s.producto_id
                    WHERE p.activo = true
                    GROUP BY p.id, p.nombre, p.sku, p.precio_venta
                    ORDER BY p.nombre
                    LIMIT 50
                """.trimIndent()) { resultSet ->
                    while (resultSet.next()) {
                        val nombre = resultSet.getString("nombre")
                        val sku = resultSet.getString("sku")
                        val precio = resultSet.getBigDecimal("precio_venta")
                        val stock = resultSet.getLong("stock_total")
                        productos.add("$nombre (SKU: $sku) - Precio: $$precio - Stock total: $stock unidades")
                    }
                }
            }
            
            if (productos.isNotEmpty()) {
                context.add("=== PRODUCTOS E INVENTARIO ===\n${productos.joinToString("\n")}")
            }
            
            // Obtener información de suscripción (solo ADMIN puede ver)
            transaction {
                exec("""
                    SELECT p.nombre as plan_nombre, p.precio_mensual, 
                           s.fecha_inicio, s.fecha_fin, s.estado
                    FROM siga_comercial.suscripciones s
                    JOIN siga_comercial.planes p ON s.plan_id = p.id
                    WHERE s.usuario_id = (SELECT id FROM siga_comercial.usuarios WHERE email = '$userEmail')
                    ORDER BY s.fecha_inicio DESC
                    LIMIT 1
                """.trimIndent()) { resultSet ->
                    if (resultSet.next()) {
                        val planNombre = resultSet.getString("plan_nombre")
                        val precio = resultSet.getBigDecimal("precio_mensual")
                        val estado = resultSet.getString("estado")
                        context.add("=== SUSCRIPCIÓN ===\nPlan: $planNombre\nPrecio: $$precio/mes\nEstado: $estado")
                    }
                }
            }
        } else {
            // OPERADOR solo ve sus locales asignados
            val locales = mutableListOf<Int>()
            transaction {
                exec("""
                    SELECT local_id 
                    FROM siga_saas.usuarios_locales 
                    WHERE usuario_id = $userId
                """.trimIndent()) { resultSet ->
                    while (resultSet.next()) {
                        locales.add(resultSet.getInt("local_id"))
                    }
                }
            }
            
            if (locales.isNotEmpty()) {
                val localesStr = locales.joinToString(", ")
                context.add("=== LOCALES ASIGNADOS ===\nLocales: $localesStr")
                
                // Obtener stock solo de sus locales
                val productos = mutableListOf<String>()
                transaction {
                    exec("""
                        SELECT p.nombre, p.sku, p.precio_venta, 
                               COALESCE(SUM(s.cantidad), 0) as stock_total,
                               l.nombre as local_nombre
                        FROM siga_saas.productos p
                        LEFT JOIN siga_saas.stock s ON p.id = s.producto_id
                        LEFT JOIN siga_saas.locales l ON s.local_id = l.id
                        WHERE p.activo = true 
                          AND (s.local_id IN ($localesStr) OR s.local_id IS NULL)
                        GROUP BY p.id, p.nombre, p.sku, p.precio_venta, l.nombre
                        ORDER BY p.nombre
                        LIMIT 50
                    """.trimIndent()) { resultSet ->
                        while (resultSet.next()) {
                            val nombre = resultSet.getString("nombre")
                            val sku = resultSet.getString("sku")
                            val precio = resultSet.getBigDecimal("precio_venta")
                            val stock = resultSet.getLong("stock_total")
                            val localNombre = resultSet.getString("local_nombre") ?: "Sin local"
                            productos.add("$nombre (SKU: $sku) - Precio: $$precio - Stock en $localNombre: $stock unidades")
                        }
                    }
                }
                
                if (productos.isNotEmpty()) {
                    context.add("=== PRODUCTOS E INVENTARIO (TUS LOCALES) ===\n${productos.joinToString("\n")}")
                }
            } else {
                context.add("=== LOCALES ASIGNADOS ===\nNo tienes locales asignados.")
            }
        }
        
        return context.joinToString("\n\n")
    }
    
    /**
     * Procesa un mensaje del usuario y genera una respuesta
     */
    suspend fun processMessage(userId: Int, userRol: String, userMessage: String): Result<String> {
        return try {
            // Construir contexto RAG
            val ragContext = buildRAGContext(userId, userRol, userMessage)
            
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

