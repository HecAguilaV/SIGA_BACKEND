package com.siga.backend.controller

import com.siga.backend.entity.EstadoSuscripcion
import com.siga.backend.entity.PeriodoSuscripcion
import com.siga.backend.entity.Suscripcion
import com.siga.backend.repository.PlanRepository
import com.siga.backend.repository.SuscripcionRepository
import com.siga.backend.repository.UsuarioComercialRepository
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
import java.time.LocalDate

data class SuscripcionRequest(
    @field:Min(1) val planId: Int,
    val periodo: String = "MENSUAL"
)

data class SuscripcionResponse(
    val id: Int,
    val usuarioId: Int,
    val planId: Int,
    val fechaInicio: String,
    val fechaFin: String?,
    val estado: String,
    val periodo: String
)

@RestController
@RequestMapping("/api/comercial/suscripciones")
@Tag(name = "3. Portal Comercial", description = "Requiere autenticación como Usuario Comercial")
class SuscripcionesController(
    private val suscripcionRepository: SuscripcionRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val planRepository: PlanRepository
) {
    
    @GetMapping
    fun listarSuscripciones(): ResponseEntity<Map<String, Any>> {
        val email = SecurityUtils.getUserEmail()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "suscripciones" to emptyList<SuscripcionResponse>(), "total" to 0))
        
        val usuario = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "suscripciones" to emptyList<SuscripcionResponse>(), "total" to 0))
        
        val suscripciones = suscripcionRepository.findByUsuarioId(usuario.id).map { s ->
            SuscripcionResponse(
                id = s.id,
                usuarioId = s.usuarioId,
                planId = s.planId,
                fechaInicio = s.fechaInicio.toString(),
                fechaFin = s.fechaFin?.toString(),
                estado = s.estado.name,
                periodo = s.periodo.name
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "suscripciones" to suscripciones,
            "total" to suscripciones.size
        ))
    }
    
    @PostMapping
    fun crearSuscripcion(@Valid @RequestBody request: SuscripcionRequest): ResponseEntity<Map<String, Any>> {
        val email = SecurityUtils.getUserEmail()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        val usuario = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        val plan = planRepository.findById(request.planId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Plan no encontrado"))
        
        val periodo = try {
            PeriodoSuscripcion.valueOf(request.periodo.uppercase())
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Periodo inválido. Debe ser MENSUAL o ANUAL"))
        }
        
        val hoy = LocalDate.now()
        val fechaFin = when (periodo) {
            PeriodoSuscripcion.MENSUAL -> hoy.plusMonths(1)
            PeriodoSuscripcion.ANUAL -> hoy.plusYears(1)
        }
        
        val nuevaSuscripcion = Suscripcion(
            usuarioId = usuario.id,
            planId = plan.id,
            fechaInicio = hoy,
            fechaFin = fechaFin,
            estado = EstadoSuscripcion.ACTIVA,
            periodo = periodo,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )
        
        val suscripcionGuardada = suscripcionRepository.save(nuevaSuscripcion)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Suscripción creada exitosamente",
            "suscripcion" to SuscripcionResponse(
                id = suscripcionGuardada.id,
                usuarioId = suscripcionGuardada.usuarioId,
                planId = suscripcionGuardada.planId,
                fechaInicio = suscripcionGuardada.fechaInicio.toString(),
                fechaFin = suscripcionGuardada.fechaFin?.toString(),
                estado = suscripcionGuardada.estado.name,
                periodo = suscripcionGuardada.periodo.name
            )
        ))
    }
}

