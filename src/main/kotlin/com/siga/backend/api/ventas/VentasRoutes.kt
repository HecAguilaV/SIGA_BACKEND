package com.siga.backend.api.ventas

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
import java.math.BigDecimal

@Serializable
data class DetalleVentaRequest(
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: String
)

@Serializable
data class VentaRequest(
    val localId: Int,
    val detalles: List<DetalleVentaRequest>,
    val observaciones: String? = null
)

@Serializable
data class DetalleVentaResponse(
    val id: Int,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: String,
    val subtotal: String
)

@Serializable
data class VentaResponse(
    val id: Int,
    val localId: Int,
    val usuarioId: Int?,
    val fecha: String,
    val total: String,
    val estado: String,
    val observaciones: String?,
    val detalles: List<DetalleVentaResponse>
)

@Serializable
data class VentasListResponse(
    val success: Boolean,
    val ventas: List<VentaResponse>,
    val total: Int
)

@Serializable
data class VentaDetailResponse(
    val success: Boolean,
    val venta: VentaResponse? = null,
    val message: String? = null
)

/**
 * Rutas para Ventas
 * Requiere autenticación JWT
 */
fun Application.configureVentasRoutes() {
    routing {
        route("/api/saas/ventas") {
            authenticate("jwt") {
                // GET /api/saas/ventas - Listar ventas
                get {
                    try {
                        val userId = call.getUserId()
                        val userRol = call.getUserRol()
                        
                        if (userId == null || userRol == null) {
                            call.respond(HttpStatusCode.Unauthorized, VentasListResponse(success = false, ventas = emptyList(), total = 0))
                            return@get
                        }
                        
                        val ventas = transaction {
                            val query = if (userRol == "OPERADOR") {
                                // OPERADOR solo ve sus ventas
                                VentaTable.select {
                                    VentaTable.usuarioId eq userId
                                }
                            } else {
                                // ADMINISTRADOR ve todas
                                VentaTable.selectAll()
                            }
                            
                            query.orderBy(VentaTable.fecha, SortOrder.DESC).limit(100).map { row ->
                                val ventaId = row[VentaTable.id]
                                val detalles = DetalleVentaTable.select {
                                    DetalleVentaTable.ventaId eq ventaId
                                }.map { detalle ->
                                    DetalleVentaResponse(
                                        id = detalle[DetalleVentaTable.id],
                                        productoId = detalle[DetalleVentaTable.productoId],
                                        cantidad = detalle[DetalleVentaTable.cantidad],
                                        precioUnitario = detalle[DetalleVentaTable.precioUnitario].toString(),
                                        subtotal = detalle[DetalleVentaTable.subtotal].toString()
                                    )
                                }
                                
                                VentaResponse(
                                    id = row[VentaTable.id],
                                    localId = row[VentaTable.localId],
                                    usuarioId = row[VentaTable.usuarioId],
                                    fecha = row[VentaTable.fecha].toString(),
                                    total = row[VentaTable.total].toString(),
                                    estado = row[VentaTable.estado],
                                    observaciones = row[VentaTable.observaciones],
                                    detalles = detalles
                                )
                            }
                        }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            VentasListResponse(success = true, ventas = ventas, total = ventas.size)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            VentasListResponse(success = false, ventas = emptyList(), total = 0)
                        )
                    }
                }
                
                // GET /api/saas/ventas/{id} - Obtener venta por ID
                get("{id}") {
                    try {
                        val ventaId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("ID inválido")
                        
                        val venta = transaction {
                            VentaTable.select {
                                VentaTable.id eq ventaId
                            }.firstOrNull()
                        }
                        
                        if (venta == null) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                VentaDetailResponse(success = false, message = "Venta no encontrada")
                            )
                            return@get
                        }
                        
                        val detalles = transaction {
                            DetalleVentaTable.select {
                                DetalleVentaTable.ventaId eq ventaId
                            }.map { detalle ->
                                DetalleVentaResponse(
                                    id = detalle[DetalleVentaTable.id],
                                    productoId = detalle[DetalleVentaTable.productoId],
                                    cantidad = detalle[DetalleVentaTable.cantidad],
                                    precioUnitario = detalle[DetalleVentaTable.precioUnitario].toString(),
                                    subtotal = detalle[DetalleVentaTable.subtotal].toString()
                                )
                            }
                        }
                        
                        val ventaResponse = VentaResponse(
                            id = venta[VentaTable.id],
                            localId = venta[VentaTable.localId],
                            usuarioId = venta[VentaTable.usuarioId],
                            fecha = venta[VentaTable.fecha].toString(),
                            total = venta[VentaTable.total].toString(),
                            estado = venta[VentaTable.estado],
                            observaciones = venta[VentaTable.observaciones],
                            detalles = detalles
                        )
                        
                        call.respond(
                            HttpStatusCode.OK,
                            VentaDetailResponse(success = true, venta = ventaResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            VentaDetailResponse(success = false, message = "Error: ${e.message}")
                        )
                    }
                }
                
                // POST /api/saas/ventas - Crear venta
                post {
                    try {
                        val userId = call.getUserId()
                            ?: throw IllegalStateException("Usuario no autenticado")
                        
                        val request = call.receive<VentaRequest>()
                        
                        if (request.detalles.isEmpty()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                VentaDetailResponse(success = false, message = "La venta debe tener al menos un producto")
                            )
                            return@post
                        }
                        
                        // Calcular total
                        val total = request.detalles.sumOf { detalle ->
                            val precio = BigDecimal(detalle.precioUnitario)
                            precio.multiply(BigDecimal(detalle.cantidad))
                        }
                        
                        val ventaId = transaction {
                            // Crear venta
                            VentaTable.insert {
                                it[VentaTable.localId] = request.localId
                                it[VentaTable.usuarioId] = userId
                                it[VentaTable.total] = total
                                it[VentaTable.estado] = "COMPLETADA"
                                it[VentaTable.observaciones] = request.observaciones
                            }
                            
                            // Obtener el ID de la venta creada
                            VentaTable.select {
                                VentaTable.localId eq request.localId
                            }.orderBy(VentaTable.fecha, SortOrder.DESC).first()[VentaTable.id]
                        }
                        
                        // Crear detalles de venta
                        transaction {
                            request.detalles.forEach { detalle ->
                                val precio = BigDecimal(detalle.precioUnitario)
                                val subtotal = precio.multiply(BigDecimal(detalle.cantidad))
                                
                                DetalleVentaTable.insert {
                                    it[DetalleVentaTable.ventaId] = ventaId
                                    it[DetalleVentaTable.productoId] = detalle.productoId
                                    it[DetalleVentaTable.cantidad] = detalle.cantidad
                                    it[DetalleVentaTable.precioUnitario] = precio
                                    it[DetalleVentaTable.subtotal] = subtotal
                                }
                            }
                        }
                        
                        // Obtener la venta creada con detalles
                        val venta = transaction {
                            VentaTable.select {
                                VentaTable.id eq ventaId
                            }.first()
                        }
                        
                        val detalles = transaction {
                            DetalleVentaTable.select {
                                DetalleVentaTable.ventaId eq ventaId
                            }.map { detalle ->
                                DetalleVentaResponse(
                                    id = detalle[DetalleVentaTable.id],
                                    productoId = detalle[DetalleVentaTable.productoId],
                                    cantidad = detalle[DetalleVentaTable.cantidad],
                                    precioUnitario = detalle[DetalleVentaTable.precioUnitario].toString(),
                                    subtotal = detalle[DetalleVentaTable.subtotal].toString()
                                )
                            }
                        }
                        
                        val ventaResponse = VentaResponse(
                            id = venta[VentaTable.id],
                            localId = venta[VentaTable.localId],
                            usuarioId = venta[VentaTable.usuarioId],
                            fecha = venta[VentaTable.fecha].toString(),
                            total = venta[VentaTable.total].toString(),
                            estado = venta[VentaTable.estado],
                            observaciones = venta[VentaTable.observaciones],
                            detalles = detalles
                        )
                        
                        call.respond(
                            HttpStatusCode.Created,
                            VentaDetailResponse(success = true, venta = ventaResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            VentaDetailResponse(success = false, message = "Error al crear venta: ${e.message}")
                        )
                    }
                }
            }
        }
    }
}

