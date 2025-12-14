package com.siga.backend.controller

import com.siga.backend.entity.Local
import com.siga.backend.repository.LocalRepository
import com.siga.backend.service.SubscriptionService
import com.siga.backend.utils.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class LocalRequest(
    @field:NotBlank val nombre: String,
    val direccion: String? = null,
    val ciudad: String? = null
)

data class LocalResponse(
    val id: Int,
    val nombre: String,
    val direccion: String?,
    val ciudad: String?,
    val activo: Boolean,
    val fechaCreacion: String
)

@RestController
@RequestMapping("/api/saas/locales")
@Tag(name = "4. Gestión Operativa", description = "Requiere autenticación + suscripción activa")
class LocalesController(
    private val localRepository: LocalRepository,
    private val subscriptionService: SubscriptionService
) {
    
    private val logger = LoggerFactory.getLogger(LocalesController::class.java)
    
    @GetMapping
    @Operation(
        summary = "Listar Locales",
        description = "Obtiene todos los locales activos. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listarLocales(): ResponseEntity<Map<String, Any>> {
        return try {
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
            
            logger.debug("Listando locales para usuario: $email")
            // Filtrar locales por empresa
            val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
            val locales = if (usuarioComercialId != null) {
                localRepository.findByActivoTrueAndUsuarioComercialId(usuarioComercialId)
            } else {
                localRepository.findByActivoTrue() // Fallback para usuarios legacy
            }.map { local ->
                LocalResponse(
                    id = local.id,
                    nombre = local.nombre,
                    direccion = local.direccion,
                    ciudad = local.ciudad,
                    activo = local.activo,
                    fechaCreacion = local.fechaCreacion.toString()
                )
            }
            
            logger.debug("Locales encontrados: ${locales.size}")
            ResponseEntity.ok(mapOf(
                "success" to true,
                "locales" to locales,
                "total" to locales.size
            ))
        } catch (e: Exception) {
            logger.error("Error al listar locales", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf(
                    "success" to false,
                    "message" to "Error al obtener locales: ${e.message}",
                    "errorType" to e.javaClass.simpleName
                ))
        }
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener Local",
        description = "Obtiene un local por ID. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun obtenerLocal(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
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
        
        val local = localRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Local no encontrado"))
        
        if (!local.activo) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Local no encontrado"))
        }
        
        // Verificar que el local pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && local.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a este local"))
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "local" to LocalResponse(
                id = local.id,
                nombre = local.nombre,
                direccion = local.direccion,
                ciudad = local.ciudad,
                activo = local.activo,
                fechaCreacion = local.fechaCreacion.toString()
            )
        ))
    }
    
    @PostMapping
    @Operation(
        summary = "Crear Local",
        description = "Crea un nuevo local. Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun crearLocal(@Valid @RequestBody request: LocalRequest): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearLocales()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para crear locales"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa"))
        }
        
        // Obtener usuario_comercial_id para asignar empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        val nuevoLocal = Local(
            nombre = request.nombre,
            direccion = request.direccion,
            ciudad = request.ciudad,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            activo = true,
            fechaCreacion = Instant.now()
        )
        
        val localGuardado = localRepository.save(nuevoLocal)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Local creado exitosamente",
            "local" to LocalResponse(
                id = localGuardado.id,
                nombre = localGuardado.nombre,
                direccion = localGuardado.direccion,
                ciudad = localGuardado.ciudad,
                activo = localGuardado.activo,
                fechaCreacion = localGuardado.fechaCreacion.toString()
            )
        ))
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar Local",
        description = "Actualiza un local existente. Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun actualizarLocal(
        @PathVariable id: Int,
        @Valid @RequestBody request: LocalRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeActualizarLocales()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para actualizar locales"))
        }
        
        val local = localRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Local no encontrado"))
        
        val localActualizado = local.copy(
            nombre = request.nombre,
            direccion = request.direccion,
            ciudad = request.ciudad
        )
        
        val localGuardado = localRepository.save(localActualizado)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Local actualizado exitosamente",
            "local" to LocalResponse(
                id = localGuardado.id,
                nombre = localGuardado.nombre,
                direccion = localGuardado.direccion,
                ciudad = localGuardado.ciudad,
                activo = localGuardado.activo,
                fechaCreacion = localGuardado.fechaCreacion.toString()
            )
        ))
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar Local",
        description = "Elimina un local (soft delete). Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun eliminarLocal(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeEliminarLocales()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para eliminar locales"))
        }
        
        val local = localRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Local no encontrado"))
        
        // Verificar que el local pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && local.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a este local"))
        }
        
        val localEliminado = local.copy(activo = false)
        localRepository.save(localEliminado)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Local eliminado exitosamente"
        ))
    }
}
