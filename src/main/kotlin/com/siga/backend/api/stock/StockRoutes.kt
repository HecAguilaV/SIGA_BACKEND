package com.siga.backend.api.stock

import com.siga.backend.config.*
import com.siga.backend.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class StockRequest(
    val productoId: Int,
    val localId: Int,
    val cantidad: Int,
    val cantidadMinima: Int? = null
)

@Serializable
data class StockResponse(
    val id: Int,
    val productoId: Int,
    val localId: Int,
    val cantidad: Int,
    val cantidadMinima: Int,
    val fechaActualizacion: String
)

@Serializable
data class StockListResponse(
    val success: Boolean,
    val stock: List<StockResponse>,
    val total: Int
)

@Serializable
data class StockDetailResponse(
    val success: Boolean,
    val stock: StockResponse? = null,
    val message: String? = null
)

/**
 * Rutas para gestión de Stock
 * Requiere autenticación JWT
 */
fun Application.configureStockRoutes() {
    routing {
        route("/api/saas/stock") {
            authenticate("jwt") {
                // GET /api/saas/stock?local_id={id} - Listar stock por local
                get {
                    try {
                        val userId = call.getUserId()
                        val userRol = call.getUserRol()
                        val localIdParam = call.request.queryParameters["local_id"]?.toIntOrNull()
                        
                        if (userId == null || userRol == null) {
                            call.respond(HttpStatusCode.Unauthorized, StockListResponse(success = false, stock = emptyList(), total = 0))
                            return@get
                        }
                        
                        // Verificar suscripción activa
                        if (!call.hasActiveSubscription()) {
                            call.respond(
                                HttpStatusCode.PaymentRequired,
                                StockListResponse(success = false, stock = emptyList(), total = 0)
                            )
                            return@get
                        }
                        
                        val stock = transaction {
                            val query = if (localIdParam != null) {
                                StockTable.select {
                                    StockTable.localId eq localIdParam
                                }
                            } else {
                                // Si es OPERADOR, solo ver sus locales asignados
                                if (userRol == "OPERADOR") {
                                    // Obtener locales del usuario
                                    val locales = mutableListOf<Int>()
                                    exec("""
                                        SELECT local_id 
                                        FROM siga_saas.usuarios_locales 
                                        WHERE usuario_id = $userId
                                    """.trimIndent()) { resultSet ->
                                        while (resultSet.next()) {
                                            locales.add(resultSet.getInt("local_id"))
                                        }
                                    }
                                    
                                    if (locales.isEmpty()) {
                                        StockTable.select { Op.FALSE } // No hay locales asignados
                                    } else {
                                        StockTable.select {
                                            StockTable.localId inList locales
                                        }
                                    }
                                } else {
                                    // ADMINISTRADOR ve todo
                                    StockTable.selectAll()
                                }
                            }
                            
                            query.map { row ->
                                StockResponse(
                                    id = row[StockTable.id],
                                    productoId = row[StockTable.productoId],
                                    localId = row[StockTable.localId],
                                    cantidad = row[StockTable.cantidad],
                                    cantidadMinima = row[StockTable.cantidadMinima],
                                    fechaActualizacion = row[StockTable.fechaActualizacion].toString()
                                )
                            }
                        }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            StockListResponse(success = true, stock = stock, total = stock.size)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            StockListResponse(success = false, stock = emptyList(), total = 0)
                        )
                    }
                }
                
                // GET /api/saas/stock/{producto_id}/{local_id} - Obtener stock específico
                get("{producto_id}/{local_id}") {
                    try {
                        // Verificar suscripción activa
                        if (!call.hasActiveSubscription()) {
                            call.respond(
                                HttpStatusCode.PaymentRequired,
                                StockDetailResponse(success = false, message = "Se requiere una suscripción activa")
                            )
                            return@get
                        }
                        
                        val productoId = call.parameters["producto_id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("ID de producto inválido")
                        val localId = call.parameters["local_id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("ID de local inválido")
                        
                        val stock = transaction {
                            StockTable.select {
                                (StockTable.productoId eq productoId) and (StockTable.localId eq localId)
                            }.firstOrNull()
                        }
                        
                        if (stock == null) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                StockDetailResponse(success = false, message = "Stock no encontrado")
                            )
                            return@get
                        }
                        
                        val stockResponse = StockResponse(
                            id = stock[StockTable.id],
                            productoId = stock[StockTable.productoId],
                            localId = stock[StockTable.localId],
                            cantidad = stock[StockTable.cantidad],
                            cantidadMinima = stock[StockTable.cantidadMinima],
                            fechaActualizacion = stock[StockTable.fechaActualizacion].toString()
                        )
                        
                        call.respond(
                            HttpStatusCode.OK,
                            StockDetailResponse(success = true, stock = stockResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            StockDetailResponse(success = false, message = "Error: ${e.message}")
                        )
                    }
                }
                
                // POST /api/saas/stock - Agregar o actualizar stock
                post {
                    try {
                        // Verificar suscripción activa
                        if (!call.hasActiveSubscription()) {
                            call.respond(
                                HttpStatusCode.PaymentRequired,
                                StockDetailResponse(success = false, message = "Se requiere una suscripción activa")
                            )
                            return@post
                        }
                        
                        val request = call.receive<StockRequest>()
                        
                        if (request.cantidad < 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                StockDetailResponse(success = false, message = "La cantidad no puede ser negativa")
                            )
                            return@post
                        }
                        
                        val stockId = transaction {
                            // Verificar si existe stock para este producto y local
                            val existing = StockTable.select {
                                (StockTable.productoId eq request.productoId) and
                                (StockTable.localId eq request.localId)
                            }.firstOrNull()
                            
                            if (existing != null) {
                                // Actualizar stock existente
                                StockTable.update({
                                    (StockTable.productoId eq request.productoId) and
                                    (StockTable.localId eq request.localId)
                                }) {
                                    it[StockTable.cantidad] = request.cantidad
                                    if (request.cantidadMinima != null) {
                                        it[StockTable.cantidadMinima] = request.cantidadMinima
                                    }
                                }
                                existing[StockTable.id]
                            } else {
                                // Crear nuevo registro de stock
                                StockTable.insert {
                                    it[StockTable.productoId] = request.productoId
                                    it[StockTable.localId] = request.localId
                                    it[StockTable.cantidad] = request.cantidad
                                    it[StockTable.cantidadMinima] = request.cantidadMinima ?: 0
                                }
                                // Obtener el ID del stock creado
                                StockTable.select {
                                    (StockTable.productoId eq request.productoId) and
                                    (StockTable.localId eq request.localId)
                                }.first()[StockTable.id]
                            }
                        }
                        
                        // Obtener el stock actualizado/creado
                        val stock = transaction {
                            StockTable.select {
                                StockTable.id eq stockId
                            }.first()
                        }
                        
                        val stockResponse = StockResponse(
                            id = stock[StockTable.id],
                            productoId = stock[StockTable.productoId],
                            localId = stock[StockTable.localId],
                            cantidad = stock[StockTable.cantidad],
                            cantidadMinima = stock[StockTable.cantidadMinima],
                            fechaActualizacion = stock[StockTable.fechaActualizacion].toString()
                        )
                        
                        call.respond(
                            HttpStatusCode.OK,
                            StockDetailResponse(success = true, stock = stockResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            StockDetailResponse(success = false, message = "Error al actualizar stock: ${e.message}")
                        )
                    }
                }
            }
        }
    }
}

