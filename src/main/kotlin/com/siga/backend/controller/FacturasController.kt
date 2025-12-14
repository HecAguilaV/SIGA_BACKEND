package com.siga.backend.controller

import com.siga.backend.service.FacturaService
import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.PlanRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import com.siga.backend.utils.SecurityUtils
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.format.DateTimeFormatter

data class CrearFacturaRequest(
    val usuarioId: Int,
    val usuarioNombre: String,
    val usuarioEmail: String,
    val planId: Int,
    val planNombre: String,
    val precioUF: BigDecimal,
    val precioCLP: BigDecimal?,
    val unidad: String = "UF",
    val fechaVencimiento: String? = null,
    val metodoPago: String? = null,
    val ultimos4Digitos: String? = null
)

data class FacturaResponse(
    val id: Int,
    val numeroFactura: String,
    val usuarioId: Int,
    val usuarioNombre: String,
    val usuarioEmail: String,
    val planId: Int,
    val planNombre: String,
    val precioUF: BigDecimal,
    val precioCLP: BigDecimal?,
    val unidad: String,
    val fechaCompra: String,
    val fechaVencimiento: String?,
    val estado: String,
    val metodoPago: String?,
    val ultimos4Digitos: String?
)

@RestController
@RequestMapping("/api/comercial/facturas")
@Tag(name = "Facturas", description = "Gestión de facturas")
class FacturasController(
    private val facturaService: FacturaService,
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val planRepository: PlanRepository
) {
    private val logger = LoggerFactory.getLogger(FacturasController::class.java)
    
    @PostMapping
    @Operation(summary = "Crear Factura", description = "Crea una nueva factura. Requiere autenticación.")
    fun crearFactura(
        @RequestBody request: CrearFacturaRequest
    ): ResponseEntity<Map<String, Any>> {
        try {
            logger.debug("Creando factura para usuario ${request.usuarioId}")
            
            // Validar que usuario y plan existen
            val usuario = usuarioComercialRepository.findById(request.usuarioId)
                .orElse(null)
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
                    "success" to false,
                    "message" to "Usuario no encontrado"
                ))
            }
            
            val plan = planRepository.findById(request.planId)
                .orElse(null)
            if (plan == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
                    "success" to false,
                    "message" to "Plan no encontrado"
                ))
            }
            
            // Parsear fecha de vencimiento si existe
            val fechaVencimiento = request.fechaVencimiento?.let {
                try {
                    Instant.parse(it)
                } catch (e: Exception) {
                    logger.warn("Error parseando fecha de vencimiento: $it", e)
                    null
                }
            }
            
            val factura = facturaService.crearFactura(
                usuarioId = request.usuarioId,
                usuarioNombre = request.usuarioNombre,
                usuarioEmail = request.usuarioEmail,
                planId = request.planId,
                planNombre = request.planNombre,
                precioUF = request.precioUF,
                precioCLP = request.precioCLP,
                unidad = request.unidad,
                fechaVencimiento = fechaVencimiento,
                metodoPago = request.metodoPago,
                ultimos4Digitos = request.ultimos4Digitos
            )
            
            val response = FacturaResponse(
                id = factura.id,
                numeroFactura = factura.numeroFactura,
                usuarioId = factura.usuarioId,
                usuarioNombre = factura.usuarioNombre,
                usuarioEmail = factura.usuarioEmail,
                planId = factura.planId,
                planNombre = factura.planNombre,
                precioUF = factura.precioUF,
                precioCLP = factura.precioCLP,
                unidad = factura.unidad,
                fechaCompra = factura.fechaCompra.toString(),
                fechaVencimiento = factura.fechaVencimiento?.toString(),
                estado = factura.estado.name.lowercase(),
                metodoPago = factura.metodoPago,
                ultimos4Digitos = factura.ultimos4Digitos
            )
            
            return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
                "success" to true,
                "message" to "Factura creada exitosamente",
                "data" to response
            ))
        } catch (e: Exception) {
            logger.error("Error al crear factura", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error al crear factura: ${e.message}"
            ))
        }
    }
    
    @GetMapping
    @Operation(summary = "Listar Facturas", description = "Obtiene todas las facturas del usuario autenticado. Requiere autenticación.")
    fun listarFacturas(): ResponseEntity<Map<String, Any>> {
        try {
            // Obtener email del usuario desde el token JWT
            val email = SecurityUtils.getUserEmail()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                    "success" to false,
                    "message" to "No autenticado"
                ))
            
            val usuario = usuarioComercialRepository.findByEmail(email.lowercase())
                .orElse(null)
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                    "success" to false,
                    "message" to "Usuario no encontrado"
                ))
            }
            
            val facturas = facturaService.obtenerFacturasPorUsuario(usuario.id)
                .map { factura ->
                    FacturaResponse(
                        id = factura.id,
                        numeroFactura = factura.numeroFactura,
                        usuarioId = factura.usuarioId,
                        usuarioNombre = factura.usuarioNombre,
                        usuarioEmail = factura.usuarioEmail,
                        planId = factura.planId,
                        planNombre = factura.planNombre,
                        precioUF = factura.precioUF,
                        precioCLP = factura.precioCLP,
                        unidad = factura.unidad,
                        fechaCompra = factura.fechaCompra.toString(),
                        fechaVencimiento = factura.fechaVencimiento?.toString(),
                        estado = factura.estado.name.lowercase(),
                        metodoPago = factura.metodoPago,
                        ultimos4Digitos = factura.ultimos4Digitos
                    )
                }
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "facturas" to facturas
            ))
        } catch (e: Exception) {
            logger.error("Error al listar facturas", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error al listar facturas: ${e.message}"
            ))
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Factura por ID", description = "Obtiene una factura específica por ID. Requiere autenticación.")
    fun obtenerFacturaPorId(
        @PathVariable id: Int
    ): ResponseEntity<Map<String, Any>> {
        try {
            val factura = facturaService.obtenerFacturaPorId(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf(
                    "success" to false,
                    "message" to "Factura no encontrada"
                ))
            
            // Verificar que la factura pertenece al usuario autenticado
            val email = SecurityUtils.getUserEmail()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                    "success" to false,
                    "message" to "No autenticado"
                ))
            
            val usuario = usuarioComercialRepository.findByEmail(email.lowercase())
                .orElse(null)
            
            if (usuario == null || factura.usuarioId != usuario.id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf(
                    "success" to false,
                    "message" to "No tienes permiso para acceder a esta factura"
                ))
            }
            
            val response = FacturaResponse(
                id = factura.id,
                numeroFactura = factura.numeroFactura,
                usuarioId = factura.usuarioId,
                usuarioNombre = factura.usuarioNombre,
                usuarioEmail = factura.usuarioEmail,
                planId = factura.planId,
                planNombre = factura.planNombre,
                precioUF = factura.precioUF,
                precioCLP = factura.precioCLP,
                unidad = factura.unidad,
                fechaCompra = factura.fechaCompra.toString(),
                fechaVencimiento = factura.fechaVencimiento?.toString(),
                estado = factura.estado.name.lowercase(),
                metodoPago = factura.metodoPago,
                ultimos4Digitos = factura.ultimos4Digitos
            )
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "factura" to response
            ))
        } catch (e: Exception) {
            logger.error("Error al obtener factura", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error al obtener factura: ${e.message}"
            ))
        }
    }
    
    @GetMapping("/numero/{numero}")
    @Operation(summary = "Obtener Factura por Número", description = "Obtiene una factura específica por número. Requiere autenticación.")
    fun obtenerFacturaPorNumero(
        @PathVariable numero: String
    ): ResponseEntity<Map<String, Any>> {
        try {
            val factura = facturaService.obtenerFacturaPorNumero(numero)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf(
                    "success" to false,
                    "message" to "Factura no encontrada"
                ))
            
            // Verificar que la factura pertenece al usuario autenticado
            val email = SecurityUtils.getUserEmail()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                    "success" to false,
                    "message" to "No autenticado"
                ))
            
            val usuario = usuarioComercialRepository.findByEmail(email.lowercase())
                .orElse(null)
            
            if (usuario == null || factura.usuarioId != usuario.id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf(
                    "success" to false,
                    "message" to "No tienes permiso para acceder a esta factura"
                ))
            }
            
            val response = FacturaResponse(
                id = factura.id,
                numeroFactura = factura.numeroFactura,
                usuarioId = factura.usuarioId,
                usuarioNombre = factura.usuarioNombre,
                usuarioEmail = factura.usuarioEmail,
                planId = factura.planId,
                planNombre = factura.planNombre,
                precioUF = factura.precioUF,
                precioCLP = factura.precioCLP,
                unidad = factura.unidad,
                fechaCompra = factura.fechaCompra.toString(),
                fechaVencimiento = factura.fechaVencimiento?.toString(),
                estado = factura.estado.name.lowercase(),
                metodoPago = factura.metodoPago,
                ultimos4Digitos = factura.ultimos4Digitos
            )
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "factura" to response
            ))
        } catch (e: Exception) {
            logger.error("Error al obtener factura por número", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error al obtener factura: ${e.message}"
            ))
        }
    }
}
