package com.siga.backend.controller

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.siga.backend.entity.Stock
import com.siga.backend.repository.StockRepository
import com.siga.backend.repository.ProductoRepository
import com.siga.backend.repository.LocalRepository
import com.siga.backend.service.SubscriptionService
import com.siga.backend.utils.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import java.time.Instant

// StockRequest acepta tanto camelCase (WebApp) como snake_case (App Móvil)
data class StockRequest(
    @field:Min(1) 
    @JsonProperty("productoId")  // Nombre principal: productoId
    @JsonAlias("producto_id")    // También acepta: producto_id
    val productoId: Int,
    
    @field:Min(1) 
    @JsonProperty("localId")     // Nombre principal: localId
    @JsonAlias("local_id")        // También acepta: local_id
    val localId: Int,
    
    @field:Min(0) 
    val cantidad: Int,
    
    @field:Min(0) 
    @JsonProperty("cantidadMinima")           // Nombre principal: cantidadMinima
    @JsonAlias("cantidad_minima", "min_stock") // También acepta: cantidad_minima o min_stock
    val cantidadMinima: Int = 0
)

data class StockResponse(
    val id: Int,
    @JsonProperty("producto_id") val productoId: Int,
    @JsonProperty("local_id") val localId: Int,
    val cantidad: Int,
    @JsonProperty("min_stock") val cantidadMinima: Int,
    @JsonProperty("fecha_actualizacion") val fechaActualizacion: String
)

@RestController
@RequestMapping("/api/saas/stock")
@Tag(name = "4. Gestión Operativa", description = "Requiere autenticación + suscripción activa")
class StockController(
    private val stockRepository: StockRepository,
    private val productoRepository: ProductoRepository,
    private val localRepository: LocalRepository,
    private val subscriptionService: SubscriptionService
) {
    
    @GetMapping
    fun listarStock(@RequestParam(required = false) localId: Int?): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
        if (userId == null || !SecurityUtils.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "stock" to emptyList<StockResponse>(), "total" to 0))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "stock" to emptyList<StockResponse>(), "total" to 0))
        }

        if (!SecurityUtils.puedeVerStock()) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para ver stock"))
        }
        
        // Filtrar stock por empresa (a través de productos y locales)
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        val stockList = if (usuarioComercialId != null) {
            if (localId != null) {
                stockRepository.findByLocalIdAndUsuarioComercialId(localId, usuarioComercialId)
            } else {
                stockRepository.findByUsuarioComercialId(usuarioComercialId)
            }
        } else {
            // Fallback para usuarios legacy
            if (localId != null) {
                stockRepository.findByLocalId(localId)
            } else {
                stockRepository.findAll()
            }
        }
        
        val stock = stockList.map { s ->
            StockResponse(
                id = s.id,
                productoId = s.productoId,
                localId = s.localId,
                cantidad = s.cantidad,
                cantidadMinima = s.cantidadMinima,
                fechaActualizacion = s.fechaActualizacion.toString()
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "stock" to stock,
            "total" to stock.size
        ))
    }
    
    @GetMapping("/{productoId}/{localId}")
    fun obtenerStock(
        @PathVariable productoId: Int,
        @PathVariable localId: Int
    ): ResponseEntity<Map<String, Any>> {
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
        
        val stock = stockRepository.findByProductoIdAndLocalId(productoId, localId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Stock no encontrado"))
        
        // Verificar que producto y local pertenecen a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null) {
            val producto = productoRepository.findById(productoId).orElse(null)
            val local = localRepository.findById(localId).orElse(null)
            
            if (producto == null || producto.usuarioComercialId != usuarioComercialId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("success" to false, "message" to "No tienes acceso a este producto"))
            }
            
            if (local == null || local.usuarioComercialId != usuarioComercialId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("success" to false, "message" to "No tienes acceso a este local"))
            }
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "stock" to StockResponse(
                id = stock.id,
                productoId = stock.productoId,
                localId = stock.localId,
                cantidad = stock.cantidad,
                cantidadMinima = stock.cantidadMinima,
                fechaActualizacion = stock.fechaActualizacion.toString()
            )
        ))
    }
    
    @PostMapping
    fun actualizarStock(@Valid @RequestBody request: StockRequest): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        }
        
        if (!SecurityUtils.puedeActualizarStock()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para actualizar stock"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa"))
        }
        
        // Verificar que producto y local pertenecen a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null) {
            val producto = productoRepository.findById(request.productoId).orElse(null)
            val local = localRepository.findById(request.localId).orElse(null)
            
            if (producto == null || producto.usuarioComercialId != usuarioComercialId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("success" to false, "message" to "No tienes acceso a este producto"))
            }
            
            if (local == null || local.usuarioComercialId != usuarioComercialId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("success" to false, "message" to "No tienes acceso a este local"))
            }
        }
        
        val stockExistente = stockRepository.findByProductoIdAndLocalId(request.productoId, request.localId)
        
        val stock = if (stockExistente.isPresent) {
            val s = stockExistente.get()
            s.copy(
                cantidad = request.cantidad,
                cantidadMinima = request.cantidadMinima,
                fechaActualizacion = Instant.now()
            )
        } else {
            Stock(
                productoId = request.productoId,
                localId = request.localId,
                cantidad = request.cantidad,
                cantidadMinima = request.cantidadMinima,
                fechaActualizacion = Instant.now()
            )
        }
        
        val stockGuardado = stockRepository.save(stock)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Stock actualizado exitosamente",
            "stock" to StockResponse(
                id = stockGuardado.id,
                productoId = stockGuardado.productoId,
                localId = stockGuardado.localId,
                cantidad = stockGuardado.cantidad,
                cantidadMinima = stockGuardado.cantidadMinima,
                fechaActualizacion = stockGuardado.fechaActualizacion.toString()
            )
        ))
    }
}

