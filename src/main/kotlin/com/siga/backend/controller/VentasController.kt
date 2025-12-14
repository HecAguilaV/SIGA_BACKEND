package com.siga.backend.controller

import com.siga.backend.entity.DetalleVenta
import com.siga.backend.entity.EstadoVenta
import com.siga.backend.entity.Venta
import com.siga.backend.repository.DetalleVentaRepository
import com.siga.backend.repository.VentaRepository
import com.siga.backend.repository.LocalRepository
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
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal
import java.time.Instant

data class DetalleVentaRequest(
    @field:Min(1) val productoId: Int,
    @field:Min(1) val cantidad: Int,
    val precioUnitario: String
)

data class VentaRequest(
    @field:Min(1) val localId: Int,
    @field:NotEmpty val detalles: List<DetalleVentaRequest>,
    val observaciones: String? = null
)

data class DetalleVentaResponse(
    val id: Int,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: String,
    val subtotal: String
)

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

@RestController
@RequestMapping("/api/saas/ventas")
@Tag(name = "4. Gestión Operativa", description = "Requiere autenticación + suscripción activa")
class VentasController(
    private val ventaRepository: VentaRepository,
    private val detalleVentaRepository: DetalleVentaRepository,
    private val localRepository: LocalRepository,
    private val productoRepository: ProductoRepository,
    private val subscriptionService: SubscriptionService
) {
    
    @GetMapping
    fun listarVentas(): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
        if (userId == null || !SecurityUtils.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "ventas" to emptyList<VentaResponse>(), "total" to 0))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "ventas" to emptyList<VentaResponse>(), "total" to 0))
        }
        
        // Filtrar ventas por empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        val ventas = if (usuarioComercialId != null) {
            ventaRepository.findByUsuarioComercialId(usuarioComercialId)
        } else {
            ventaRepository.findAll() // Fallback para usuarios legacy
        }.map { venta ->
            val detalles = detalleVentaRepository.findAll().filter { it.ventaId == venta.id }
            VentaResponse(
                id = venta.id,
                localId = venta.localId,
                usuarioId = venta.usuarioId,
                fecha = venta.fecha.toString(),
                total = venta.total.toString(),
                estado = venta.estado.name,
                observaciones = venta.observaciones,
                detalles = detalles.map { d ->
                    DetalleVentaResponse(
                        id = d.id,
                        productoId = d.productoId,
                        cantidad = d.cantidad,
                        precioUnitario = d.precioUnitario.toString(),
                        subtotal = d.subtotal.toString()
                    )
                }
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "ventas" to ventas,
            "total" to ventas.size
        ))
    }
    
    @PostMapping
    fun crearVenta(@Valid @RequestBody request: VentaRequest): ResponseEntity<Map<String, Any>> {
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
        
        // Verificar que el local pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null) {
            val local = localRepository.findById(request.localId).orElse(null)
            if (local == null || local.usuarioComercialId != usuarioComercialId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("success" to false, "message" to "No tienes acceso a este local"))
            }
            
            // Verificar que todos los productos pertenecen a la empresa
            for (detalle in request.detalles) {
                val producto = productoRepository.findById(detalle.productoId).orElse(null)
                if (producto == null || producto.usuarioComercialId != usuarioComercialId) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(mapOf("success" to false, "message" to "No tienes acceso a uno o más productos"))
                }
            }
        }
        
        val total = request.detalles.sumOf { detalle ->
            BigDecimal(detalle.precioUnitario) * BigDecimal(detalle.cantidad)
        }
        
        val venta = Venta(
            localId = request.localId,
            usuarioId = userId,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            fecha = Instant.now(),
            total = total,
            estado = EstadoVenta.COMPLETADA,
            observaciones = request.observaciones
        )
        
        val ventaGuardada = ventaRepository.save(venta)
        
        val detalles = request.detalles.map { detalle ->
            val precioUnitario = BigDecimal(detalle.precioUnitario)
            val subtotal = precioUnitario * BigDecimal(detalle.cantidad)
            
            DetalleVenta(
                ventaId = ventaGuardada.id,
                productoId = detalle.productoId,
                cantidad = detalle.cantidad,
                precioUnitario = precioUnitario,
                subtotal = subtotal
            )
        }
        
        val detallesGuardados = detalleVentaRepository.saveAll(detalles)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Venta creada exitosamente",
            "venta" to VentaResponse(
                id = ventaGuardada.id,
                localId = ventaGuardada.localId,
                usuarioId = ventaGuardada.usuarioId,
                fecha = ventaGuardada.fecha.toString(),
                total = ventaGuardada.total.toString(),
                estado = ventaGuardada.estado.name,
                observaciones = ventaGuardada.observaciones,
                detalles = detallesGuardados.map { d ->
                    DetalleVentaResponse(
                        id = d.id,
                        productoId = d.productoId,
                        cantidad = d.cantidad,
                        precioUnitario = d.precioUnitario.toString(),
                        subtotal = d.subtotal.toString()
                    )
                }
            )
        ))
    }
}

