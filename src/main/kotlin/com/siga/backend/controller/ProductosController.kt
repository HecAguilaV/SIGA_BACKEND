package com.siga.backend.controller

import com.siga.backend.entity.Producto
import com.siga.backend.repository.ProductoRepository
import com.siga.backend.service.SubscriptionService
import com.siga.backend.utils.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant

data class ProductoRequest(
    @field:NotBlank val nombre: String,
    val descripcion: String? = null,
    val categoriaId: Int? = null,
    val codigoBarras: String? = null,
    val precioUnitario: String? = null
)

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

@RestController
@RequestMapping("/api/saas/productos")
@Tag(name = "4. Gestión Operativa", description = "Requiere autenticación + suscripción activa")
class ProductosController(
    private val productoRepository: ProductoRepository,
    private val subscriptionService: SubscriptionService
) {
    
    @GetMapping
    fun listarProductos(): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
        if (userId == null || !SecurityUtils.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa"))
        }
        
        // Filtrar productos por empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        val productos = if (usuarioComercialId != null) {
            productoRepository.findByActivoTrueAndUsuarioComercialId(usuarioComercialId)
        } else {
            productoRepository.findByActivoTrue() // Fallback para usuarios legacy
        }.map { producto ->
            ProductoResponse(
                id = producto.id,
                nombre = producto.nombre,
                descripcion = producto.descripcion,
                categoriaId = producto.categoriaId,
                codigoBarras = producto.codigoBarras,
                precioUnitario = producto.precioUnitario?.toString(),
                activo = producto.activo,
                fechaCreacion = producto.fechaCreacion.toString(),
                fechaActualizacion = producto.fechaActualizacion.toString()
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "productos" to productos,
            "total" to productos.size
        ))
    }
    
    @GetMapping("/{id}")
    fun obtenerProducto(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
        if (userId == null || !SecurityUtils.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa"))
        }
        
        val producto = productoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Producto no encontrado"))
        
        if (!producto.activo) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Producto no encontrado"))
        }
        
        // Verificar que el producto pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && producto.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a este producto"))
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "producto" to ProductoResponse(
                id = producto.id,
                nombre = producto.nombre,
                descripcion = producto.descripcion,
                categoriaId = producto.categoriaId,
                codigoBarras = producto.codigoBarras,
                precioUnitario = producto.precioUnitario?.toString(),
                activo = producto.activo,
                fechaCreacion = producto.fechaCreacion.toString(),
                fechaActualizacion = producto.fechaActualizacion.toString()
            )
        ))
    }
    
    @PostMapping
    fun crearProducto(@Valid @RequestBody request: ProductoRequest): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearProductos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para crear productos"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa"))
        }
        
        val precioUnitario = request.precioUnitario?.let { 
            try { BigDecimal(it) } catch (e: Exception) { null }
        }
        
        // Obtener usuario_comercial_id para asignar empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        val nuevoProducto = Producto(
            nombre = request.nombre,
            descripcion = request.descripcion,
            categoriaId = request.categoriaId,
            codigoBarras = request.codigoBarras,
            precioUnitario = precioUnitario,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )
        
        val productoGuardado = productoRepository.save(nuevoProducto)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Producto creado exitosamente",
            "producto" to ProductoResponse(
                id = productoGuardado.id,
                nombre = productoGuardado.nombre,
                descripcion = productoGuardado.descripcion,
                categoriaId = productoGuardado.categoriaId,
                codigoBarras = productoGuardado.codigoBarras,
                precioUnitario = productoGuardado.precioUnitario?.toString(),
                activo = productoGuardado.activo,
                fechaCreacion = productoGuardado.fechaCreacion.toString(),
                fechaActualizacion = productoGuardado.fechaActualizacion.toString()
            )
        ))
    }
    
    @PutMapping("/{id}")
    fun actualizarProducto(
        @PathVariable id: Int,
        @Valid @RequestBody request: ProductoRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeActualizarProductos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para actualizar productos"))
        }
        
        val producto = productoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Producto no encontrado"))
        
        // Verificar que el producto pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && producto.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a este producto"))
        }
        
        val precioUnitario = request.precioUnitario?.let { 
            try { BigDecimal(it) } catch (e: Exception) { null }
        }
        
        val productoActualizado = producto.copy(
            nombre = request.nombre,
            descripcion = request.descripcion,
            categoriaId = request.categoriaId,
            codigoBarras = request.codigoBarras,
            precioUnitario = precioUnitario,
            fechaActualizacion = Instant.now()
        )
        
        val productoGuardado = productoRepository.save(productoActualizado)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Producto actualizado exitosamente",
            "producto" to ProductoResponse(
                id = productoGuardado.id,
                nombre = productoGuardado.nombre,
                descripcion = productoGuardado.descripcion,
                categoriaId = productoGuardado.categoriaId,
                codigoBarras = productoGuardado.codigoBarras,
                precioUnitario = productoGuardado.precioUnitario?.toString(),
                activo = productoGuardado.activo,
                fechaCreacion = productoGuardado.fechaCreacion.toString(),
                fechaActualizacion = productoGuardado.fechaActualizacion.toString()
            )
        ))
    }
    
    @DeleteMapping("/{id}")
    fun eliminarProducto(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeEliminarProductos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para eliminar productos"))
        }
        
        val producto = productoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Producto no encontrado"))
        
        // Verificar que el producto pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && producto.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a este producto"))
        }
        
        val productoEliminado = producto.copy(activo = false, fechaActualizacion = Instant.now())
        productoRepository.save(productoEliminado)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Producto eliminado exitosamente"
        ))
    }
}

