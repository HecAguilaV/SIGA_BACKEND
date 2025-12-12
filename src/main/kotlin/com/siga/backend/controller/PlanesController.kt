package com.siga.backend.controller

import com.siga.backend.entity.Plan
import com.siga.backend.repository.PlanRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class PlanResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,  // Según especificación
    val unidad: String = "UF",  // Según especificación
    val esFreemium: Boolean,  // Según especificación
    val activo: Boolean,
    val caracteristicas: List<String>,  // Array según especificación
    val limiteBodegas: Int?,
    val limiteUsuarios: Int?,
    val limiteProductos: Int?
)

@RestController
@RequestMapping("/api/comercial/planes")
@Tag(name = "1. Público - Sin Autenticación", description = "Endpoints públicos")
class PlanesController(
    private val planRepository: PlanRepository,
    private val objectMapper: ObjectMapper
) {
    
    private fun parsearCaracteristicas(caracteristicasJson: String?): List<String> {
        if (caracteristicasJson == null) return emptyList()
        return try {
            val jsonNode = objectMapper.readTree(caracteristicasJson)
            // Extraer características del JSON (por ahora del JSONB, en futuro de tabla separada)
            // Por ahora retornamos lista vacía, se puede mejorar parseando el JSON
            emptyList() // TODO: Parsear características del JSONB
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun esFreemium(precio: java.math.BigDecimal): Boolean {
        return precio.compareTo(java.math.BigDecimal.ZERO) == 0
    }
    
    private fun convertirPlan(plan: Plan): PlanResponse {
        val caracteristicas = parsearCaracteristicas(plan.caracteristicas)
        return PlanResponse(
            id = plan.id,
            nombre = plan.nombre,
            descripcion = plan.descripcion,
            precio = plan.precioMensual.toDouble(),
            unidad = "UF",
            esFreemium = esFreemium(plan.precioMensual),
            activo = plan.activo,
            caracteristicas = caracteristicas,
            limiteBodegas = plan.limiteBodegas,
            limiteUsuarios = plan.limiteUsuarios,
            limiteProductos = plan.limiteProductos
        )
    }
    
    @GetMapping
    @Operation(summary = "Listar Planes", description = "Obtiene todos los planes de suscripción disponibles. NO requiere autenticación.")
    fun listarPlanes(): ResponseEntity<Map<String, Any>> {
        val planes = planRepository.findByActivoTrueOrderByOrdenAsc().map { plan ->
            convertirPlan(plan)
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "planes" to planes,
            "total" to planes.size
        ))
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Plan por ID", description = "Obtiene un plan específico por ID. NO requiere autenticación.")
    fun obtenerPlan(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val plan = planRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(404).body(mapOf(
                "success" to false,
                "message" to "Plan no encontrado"
            ))
        
        if (!plan.activo) {
            return ResponseEntity.status(404).body(mapOf(
                "success" to false,
                "message" to "Plan no encontrado"
            ))
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "plan" to convertirPlan(plan)
        ))
    }
}

