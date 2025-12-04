package com.siga.backend.controller

import com.siga.backend.entity.Plan
import com.siga.backend.repository.PlanRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class PlanResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precioMensual: String,
    val precioAnual: String?,
    val limiteBodegas: Int,
    val limiteUsuarios: Int,
    val limiteProductos: Int?,
    val activo: Boolean
)

@RestController
@RequestMapping("/api/comercial/planes")
@Tag(name = "1. Público - Sin Autenticación", description = "Endpoints públicos")
class PlanesController(
    private val planRepository: PlanRepository
) {
    
    @GetMapping
    @Operation(summary = "Listar Planes", description = "Obtiene todos los planes de suscripción disponibles. NO requiere autenticación.")
    fun listarPlanes(): ResponseEntity<Map<String, Any>> {
        val planes = planRepository.findByActivoTrueOrderByOrdenAsc().map { plan ->
            PlanResponse(
                id = plan.id,
                nombre = plan.nombre,
                descripcion = plan.descripcion,
                precioMensual = plan.precioMensual.toString(),
                precioAnual = plan.precioAnual?.toString(),
                limiteBodegas = plan.limiteBodegas,
                limiteUsuarios = plan.limiteUsuarios,
                limiteProductos = plan.limiteProductos,
                activo = plan.activo
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "planes" to planes,
            "total" to planes.size
        ))
    }
    
    @GetMapping("/{id}")
    fun obtenerPlan(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val plan = planRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        
        if (!plan.activo) {
            return ResponseEntity.notFound().build()
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "plan" to PlanResponse(
                id = plan.id,
                nombre = plan.nombre,
                descripcion = plan.descripcion,
                precioMensual = plan.precioMensual.toString(),
                precioAnual = plan.precioAnual?.toString(),
                limiteBodegas = plan.limiteBodegas,
                limiteUsuarios = plan.limiteUsuarios,
                limiteProductos = plan.limiteProductos,
                activo = plan.activo
            )
        ))
    }
}

