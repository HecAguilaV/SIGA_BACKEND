package com.siga.backend.api.productos

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
import java.time.Instant

@Serializable
data class ProductoRequest(
    val nombre: String,
    val descripcion: String? = null,
    val categoriaId: Int? = null,
    val codigoBarras: String? = null,
    val precioUnitario: String? = null // Recibido como String, se convierte a BigDecimal
)

@Serializable
data class ProductoResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val categoriaId: Int?,
    val codigoBarras: String?,
    val precioUnitario: String?,
    val activo: Boolean,
    val fechaCreacion: String,
    val fechaActualizacion: String
)

@Serializable
data class ProductosListResponse(
    val success: Boolean,
    val productos: List<ProductoResponse>,
    val total: Int
)

@Serializable
data class ProductoDetailResponse(
    val success: Boolean,
    val producto: ProductoResponse? = null,
    val message: String? = null
)

/**
 * Rutas CRUD para Productos
 * Requiere autenticación JWT
 */
fun Application.configureProductosRoutes() {
    routing {
        route("/api/saas/productos") {
            authenticate("jwt") {
                // GET /api/saas/productos - Listar productos
                get {
                    try {
                        val userId = call.getUserId()
                        val userRol = call.getUserRol()
                        
                        if (userId == null || userRol == null) {
                            call.respond(HttpStatusCode.Unauthorized, ProductoDetailResponse(success = false, message = "No autenticado"))
                            return@get
                        }
                        
                        val productos = transaction {
                            ProductoTable.select {
                                ProductoTable.activo eq true
                            }.map { row ->
                                ProductoResponse(
                                    id = row[ProductoTable.id],
                                    nombre = row[ProductoTable.nombre],
                                    descripcion = row[ProductoTable.descripcion],
                                    categoriaId = row[ProductoTable.categoriaId],
                                    codigoBarras = row[ProductoTable.codigoBarras],
                                    precioUnitario = row[ProductoTable.precioUnitario]?.toString(),
                                    activo = row[ProductoTable.activo],
                                    fechaCreacion = row[ProductoTable.fechaCreacion].toString(),
                                    fechaActualizacion = row[ProductoTable.fechaActualizacion].toString()
                                )
                            }
                        }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            ProductosListResponse(success = true, productos = productos, total = productos.size)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ProductosListResponse(success = false, productos = emptyList(), total = 0)
                        )
                    }
                }
                
                // GET /api/saas/productos/{id} - Obtener producto por ID
                get("{id}") {
                    try {
                        val productoId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("ID inválido")
                        
                        val producto = transaction {
                            ProductoTable.select {
                                (ProductoTable.id eq productoId) and (ProductoTable.activo eq true)
                            }.firstOrNull()
                        }
                        
                        if (producto == null) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ProductoDetailResponse(success = false, message = "Producto no encontrado")
                            )
                            return@get
                        }
                        
                        val productoResponse = ProductoResponse(
                            id = producto[ProductoTable.id],
                            nombre = producto[ProductoTable.nombre],
                            descripcion = producto[ProductoTable.descripcion],
                            categoriaId = producto[ProductoTable.categoriaId],
                            codigoBarras = producto[ProductoTable.codigoBarras],
                            precioUnitario = producto[ProductoTable.precioUnitario]?.toString(),
                            activo = producto[ProductoTable.activo],
                            fechaCreacion = producto[ProductoTable.fechaCreacion].toString(),
                            fechaActualizacion = producto[ProductoTable.fechaActualizacion].toString()
                        )
                        
                        call.respond(
                            HttpStatusCode.OK,
                            ProductoDetailResponse(success = true, producto = productoResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ProductoDetailResponse(success = false, message = "Error: ${e.message}")
                        )
                    }
                }
                
                // POST /api/saas/productos - Crear producto
                post {
                    try {
                        // Solo ADMINISTRADOR puede crear productos
                        if (!call.isAdmin()) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ProductoDetailResponse(success = false, message = "Solo administradores pueden crear productos")
                            )
                            return@post
                        }
                        
                        val request = call.receive<ProductoRequest>()
                        
                        val precioUnitario = request.precioUnitario?.let { 
                            try { BigDecimal(it) } catch (e: Exception) { null }
                        }
                        
                        val productoId = transaction {
                            ProductoTable.insert {
                                it[ProductoTable.nombre] = request.nombre
                                it[ProductoTable.descripcion] = request.descripcion
                                it[ProductoTable.categoriaId] = request.categoriaId
                                it[ProductoTable.codigoBarras] = request.codigoBarras
                                it[ProductoTable.precioUnitario] = precioUnitario
                                it[ProductoTable.activo] = true
                            }
                            // Obtener el id del producto recién creado
                            ProductoTable.select {
                                ProductoTable.nombre eq request.nombre
                            }.first()[ProductoTable.id]
                        }
                        
                        // Obtener el producto creado
                        val producto = transaction {
                            ProductoTable.select {
                                ProductoTable.id eq productoId
                            }.first()
                        }
                        
                        val productoResponse = ProductoResponse(
                            id = producto[ProductoTable.id],
                            nombre = producto[ProductoTable.nombre],
                            descripcion = producto[ProductoTable.descripcion],
                            categoriaId = producto[ProductoTable.categoriaId],
                            codigoBarras = producto[ProductoTable.codigoBarras],
                            precioUnitario = producto[ProductoTable.precioUnitario]?.toString(),
                            activo = producto[ProductoTable.activo],
                            fechaCreacion = producto[ProductoTable.fechaCreacion].toString(),
                            fechaActualizacion = producto[ProductoTable.fechaActualizacion].toString()
                        )
                        
                        call.respond(
                            HttpStatusCode.Created,
                            ProductoDetailResponse(success = true, producto = productoResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ProductoDetailResponse(success = false, message = "Error al crear producto: ${e.message}")
                        )
                    }
                }
                
                // PUT /api/saas/productos/{id} - Actualizar producto
                put("{id}") {
                    try {
                        // Solo ADMINISTRADOR puede actualizar productos
                        if (!call.isAdmin()) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ProductoDetailResponse(success = false, message = "Solo administradores pueden actualizar productos")
                            )
                            return@put
                        }
                        
                        val productoId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("ID inválido")
                        
                        val request = call.receive<ProductoRequest>()
                        
                        val precioUnitario = request.precioUnitario?.let { 
                            try { BigDecimal(it) } catch (e: Exception) { null }
                        }
                        
                        val updated = transaction {
                            ProductoTable.update({ ProductoTable.id eq productoId }) {
                                it[ProductoTable.nombre] = request.nombre
                                it[ProductoTable.descripcion] = request.descripcion
                                it[ProductoTable.categoriaId] = request.categoriaId
                                it[ProductoTable.codigoBarras] = request.codigoBarras
                                it[ProductoTable.precioUnitario] = precioUnitario
                            }
                        }
                        
                        if (updated == 0) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ProductoDetailResponse(success = false, message = "Producto no encontrado")
                            )
                            return@put
                        }
                        
                        // Obtener el producto actualizado
                        val producto = transaction {
                            ProductoTable.select {
                                ProductoTable.id eq productoId
                            }.first()
                        }
                        
                        val productoResponse = ProductoResponse(
                            id = producto[ProductoTable.id],
                            nombre = producto[ProductoTable.nombre],
                            descripcion = producto[ProductoTable.descripcion],
                            categoriaId = producto[ProductoTable.categoriaId],
                            codigoBarras = producto[ProductoTable.codigoBarras],
                            precioUnitario = producto[ProductoTable.precioUnitario]?.toString(),
                            activo = producto[ProductoTable.activo],
                            fechaCreacion = producto[ProductoTable.fechaCreacion].toString(),
                            fechaActualizacion = producto[ProductoTable.fechaActualizacion].toString()
                        )
                        
                        call.respond(
                            HttpStatusCode.OK,
                            ProductoDetailResponse(success = true, producto = productoResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ProductoDetailResponse(success = false, message = "Error al actualizar producto: ${e.message}")
                        )
                    }
                }
                
                // DELETE /api/saas/productos/{id} - Eliminar producto (soft delete)
                delete("{id}") {
                    try {
                        // Solo ADMINISTRADOR puede eliminar productos
                        if (!call.isAdmin()) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ProductoDetailResponse(success = false, message = "Solo administradores pueden eliminar productos")
                            )
                            return@delete
                        }
                        
                        val productoId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("ID inválido")
                        
                        val updated = transaction {
                            ProductoTable.update({ ProductoTable.id eq productoId }) {
                                it[ProductoTable.activo] = false
                            }
                        }
                        
                        if (updated == 0) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ProductoDetailResponse(success = false, message = "Producto no encontrado")
                            )
                            return@delete
                        }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            ProductoDetailResponse(success = true, message = "Producto eliminado correctamente")
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ProductoDetailResponse(success = false, message = "Error al eliminar producto: ${e.message}")
                        )
                    }
                }
            }
        }
    }
}

