package com.siga.backend.controller

import com.siga.backend.entity.Categoria
import com.siga.backend.repository.CategoriaRepository
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

data class CategoriaRequest(
    @field:NotBlank val nombre: String,
    val descripcion: String? = null
)

data class CategoriaResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val activa: Boolean,
    val fechaCreacion: String
)

@RestController
@RequestMapping("/api/saas/categorias")
@Tag(name = "4. Gestión Operativa", description = "Requiere autenticación + suscripción activa")
class CategoriasController(
    private val categoriaRepository: CategoriaRepository,
    private val subscriptionService: SubscriptionService
) {
    
    private val logger = LoggerFactory.getLogger(CategoriasController::class.java)
    
    @GetMapping
    @Operation(
        summary = "Listar Categorías",
        description = "Obtiene todas las categorías activas. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listarCategorias(): ResponseEntity<Map<String, Any>> {
        return try {
            val userId = SecurityUtils.getUserId()
            if (userId == null || !SecurityUtils.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("success" to false, "message" to "No autenticado"))
            }
            
            val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
            if (usuarioComercialId == null || !subscriptionService.hasActiveSubscription(usuarioComercialId)) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa (Empresa)"))
            }

            if (!SecurityUtils.puedeVerCategorias()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("success" to false, "message" to "No tienes permiso para ver categorías"))
            }
            
            // Filtrar categorías por empresa
            val categorias = if (usuarioComercialId != null) {
                categoriaRepository.findByActivaTrueAndUsuarioComercialId(usuarioComercialId)
            } else {
                categoriaRepository.findByActivaTrue() // Fallback para usuarios legacy
            }.map { categoria ->
                CategoriaResponse(
                    id = categoria.id,
                    nombre = categoria.nombre,
                    descripcion = categoria.descripcion,
                    activa = categoria.activa,
                    fechaCreacion = categoria.fechaCreacion.toString()
                )
            }
            
            logger.debug("Categorías encontradas: ${categorias.size}")
            ResponseEntity.ok(mapOf(
                "success" to true,
                "categorias" to categorias,
                "total" to categorias.size
            ))
        } catch (e: Exception) {
            logger.error("Error al listar categorías", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf(
                    "success" to false,
                    "message" to "Error al obtener categorías: ${e.message}",
                    "errorType" to e.javaClass.simpleName
                ))
        }
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener Categoría",
        description = "Obtiene una categoría por ID. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun obtenerCategoria(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
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
        
        val categoria = categoriaRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Categoría no encontrada"))
        
        if (!categoria.activa) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Categoría no encontrada"))
        }
        
        // Verificar que la categoría pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && categoria.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a esta categoría"))
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "categoria" to CategoriaResponse(
                id = categoria.id,
                nombre = categoria.nombre,
                descripcion = categoria.descripcion,
                activa = categoria.activa,
                fechaCreacion = categoria.fechaCreacion.toString()
            )
        ))
    }
    
    @PostMapping
    @Operation(
        summary = "Crear Categoría",
        description = "Crea una nueva categoría. Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun crearCategoria(@Valid @RequestBody request: CategoriaRequest): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearCategorias()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para crear categorías"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa"))
        }
        
        // Obtener usuario_comercial_id para asignar empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        // Verificar que no exista categoría con mismo nombre en la misma empresa
        if (usuarioComercialId != null && categoriaRepository.existsByNombreAndUsuarioComercialId(request.nombre, usuarioComercialId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "Ya existe una categoría con ese nombre"))
        } else if (usuarioComercialId == null && categoriaRepository.existsByNombre(request.nombre)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "Ya existe una categoría con ese nombre"))
        }
        
        val nuevaCategoria = Categoria(
            nombre = request.nombre,
            descripcion = request.descripcion,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            activa = true,
            fechaCreacion = Instant.now()
        )
        
        val categoriaGuardada = categoriaRepository.save(nuevaCategoria)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Categoría creada exitosamente",
            "categoria" to CategoriaResponse(
                id = categoriaGuardada.id,
                nombre = categoriaGuardada.nombre,
                descripcion = categoriaGuardada.descripcion,
                activa = categoriaGuardada.activa,
                fechaCreacion = categoriaGuardada.fechaCreacion.toString()
            )
        ))
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar Categoría",
        description = "Actualiza una categoría existente. Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun actualizarCategoria(
        @PathVariable id: Int,
        @Valid @RequestBody request: CategoriaRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeActualizarCategorias()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para actualizar categorías"))
        }
        
        val categoria = categoriaRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Categoría no encontrada"))
        
        // Verificar que la categoría pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && categoria.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a esta categoría"))
        }
        
        // Verificar si el nombre ya existe en otra categoría de la misma empresa
        if (request.nombre != categoria.nombre) {
            if (usuarioComercialId != null && categoriaRepository.existsByNombreAndUsuarioComercialId(request.nombre, usuarioComercialId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(mapOf("success" to false, "message" to "Ya existe una categoría con ese nombre"))
            } else if (usuarioComercialId == null && categoriaRepository.existsByNombre(request.nombre)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(mapOf("success" to false, "message" to "Ya existe una categoría con ese nombre"))
            }
        }
        
        val categoriaActualizada = categoria.copy(
            nombre = request.nombre,
            descripcion = request.descripcion
        )
        
        val categoriaGuardada = categoriaRepository.save(categoriaActualizada)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Categoría actualizada exitosamente",
            "categoria" to CategoriaResponse(
                id = categoriaGuardada.id,
                nombre = categoriaGuardada.nombre,
                descripcion = categoriaGuardada.descripcion,
                activa = categoriaGuardada.activa,
                fechaCreacion = categoriaGuardada.fechaCreacion.toString()
            )
        ))
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar Categoría",
        description = "Elimina una categoría (soft delete). Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun eliminarCategoria(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeEliminarCategorias()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para eliminar categorías"))
        }
        
        val categoria = categoriaRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Categoría no encontrada"))
        
        // Verificar que la categoría pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && categoria.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a esta categoría"))
        }
        
        val categoriaEliminada = categoria.copy(activa = false)
        categoriaRepository.save(categoriaEliminada)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Categoría eliminada exitosamente"
        ))
    }
}
