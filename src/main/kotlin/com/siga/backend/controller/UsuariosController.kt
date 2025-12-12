package com.siga.backend.controller

import com.siga.backend.entity.Rol
import com.siga.backend.entity.UsuarioSaas
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.service.PermisosService
import com.siga.backend.service.PasswordService
import com.siga.backend.utils.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CrearUsuarioRequest(
    @field:NotBlank val nombre: String,
    val apellido: String? = null,
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val rol: String
)

data class ActualizarUsuarioRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val activo: Boolean? = null
)

data class UsuarioResponse(
    val id: Int,
    val email: String,
    val nombre: String,
    val apellido: String?,
    val rol: String,
    val activo: Boolean,
    val permisos: List<String> = emptyList()
)

data class AsignarPermisoRequest(
    @field:NotBlank val codigoPermiso: String
)

@RestController
@RequestMapping("/api/saas/usuarios")
@Tag(name = "4. Gestión Operativa", description = "Gestión de usuarios operativos y permisos. Requiere autenticación + suscripción activa")
class UsuariosController(
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val permisosService: PermisosService,
    private val passwordService: PasswordService
) {
    
    @GetMapping
    @Operation(
        summary = "Listar Usuarios Operativos",
        description = "Lista todos los usuarios operativos. Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listarUsuarios(): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para ver usuarios"))
        }
        
        val usuarios = usuarioSaasRepository.findAll().map { usuario ->
            val permisos = permisosService.obtenerPermisosUsuario(usuario.id)
            UsuarioResponse(
                id = usuario.id,
                email = usuario.email,
                nombre = usuario.nombre,
                apellido = usuario.apellido,
                rol = usuario.rol.name,
                activo = usuario.activo,
                permisos = permisos
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "usuarios" to usuarios,
            "total" to usuarios.size
        ))
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener Usuario",
        description = "Obtiene información de un usuario operativo específico. Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun obtenerUsuario(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para ver usuarios"))
        }
        
        val usuario = usuarioSaasRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        val permisos = permisosService.obtenerPermisosUsuario(usuario.id)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "usuario" to UsuarioResponse(
                id = usuario.id,
                email = usuario.email,
                nombre = usuario.nombre,
                apellido = usuario.apellido,
                rol = usuario.rol.name,
                activo = usuario.activo,
                permisos = permisos
            )
        ))
    }
    
    @PostMapping
    @Operation(
        summary = "Crear Usuario Operativo",
        description = "Crea un nuevo usuario operativo. Solo administradores. Requiere autenticación y suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun crearUsuario(@Valid @RequestBody request: CrearUsuarioRequest): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para crear usuarios"))
        }
        
        val rol = try {
            Rol.valueOf(request.rol.uppercase())
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Rol inválido. Debe ser: ADMINISTRADOR, OPERADOR o CAJERO"))
        }
        
        if (usuarioSaasRepository.existsByEmail(request.email.lowercase())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "El email ya está registrado"))
        }
        
        val passwordHash = passwordService.hashPassword(request.password)
        val nuevoUsuario = UsuarioSaas(
            email = request.email.lowercase(),
            passwordHash = passwordHash,
            nombre = request.nombre,
            apellido = request.apellido,
            rol = rol,
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )
        
        val usuarioGuardado = usuarioSaasRepository.save(nuevoUsuario)
        val permisos = permisosService.obtenerPermisosUsuario(usuarioGuardado.id)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
            "success" to true,
            "message" to "Usuario creado exitosamente",
            "usuario" to UsuarioResponse(
                id = usuarioGuardado.id,
                email = usuarioGuardado.email,
                nombre = usuarioGuardado.nombre,
                apellido = usuarioGuardado.apellido,
                rol = usuarioGuardado.rol.name,
                activo = usuarioGuardado.activo,
                permisos = permisos
            )
        ))
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar Usuario",
        description = "Actualiza información de un usuario operativo. Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun actualizarUsuario(
        @PathVariable id: Int,
        @Valid @RequestBody request: ActualizarUsuarioRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para actualizar usuarios"))
        }
        
        val usuario = usuarioSaasRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        val usuarioActualizado = usuario.copy(
            nombre = request.nombre ?: usuario.nombre,
            apellido = request.apellido ?: usuario.apellido,
            activo = request.activo ?: usuario.activo,
            fechaActualizacion = Instant.now()
        )
        
        val usuarioGuardado = usuarioSaasRepository.save(usuarioActualizado)
        val permisos = permisosService.obtenerPermisosUsuario(usuarioGuardado.id)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Usuario actualizado exitosamente",
            "usuario" to UsuarioResponse(
                id = usuarioGuardado.id,
                email = usuarioGuardado.email,
                nombre = usuarioGuardado.nombre,
                apellido = usuarioGuardado.apellido,
                rol = usuarioGuardado.rol.name,
                activo = usuarioGuardado.activo,
                permisos = permisos
            )
        ))
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Desactivar Usuario",
        description = "Desactiva un usuario operativo (soft delete). Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun desactivarUsuario(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para desactivar usuarios"))
        }
        
        val usuario = usuarioSaasRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        val usuarioDesactivado = usuario.copy(activo = false, fechaActualizacion = Instant.now())
        usuarioSaasRepository.save(usuarioDesactivado)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Usuario desactivado exitosamente"
        ))
    }
    
    @GetMapping("/{id}/permisos")
    @Operation(
        summary = "Obtener Permisos de Usuario",
        description = "Obtiene todos los permisos de un usuario (rol + adicionales). Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun obtenerPermisosUsuario(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeAsignarPermisos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para ver permisos"))
        }
        
        val usuario = usuarioSaasRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        val permisos = permisosService.obtenerPermisosUsuario(id)
        val permisosPorCategoria = permisosService.obtenerPermisosUsuarioPorCategoria(id)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "usuario" to mapOf(
                "id" to usuario.id,
                "email" to usuario.email,
                "nombre" to usuario.nombre,
                "rol" to usuario.rol.name
            ),
            "permisos" to permisos,
            "permisosPorCategoria" to permisosPorCategoria
        ))
    }
    
    @PostMapping("/{id}/permisos")
    @Operation(
        summary = "Asignar Permiso a Usuario",
        description = "Asigna un permiso adicional a un usuario. Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun asignarPermiso(
        @PathVariable id: Int,
        @Valid @RequestBody request: AsignarPermisoRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeAsignarPermisos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para asignar permisos"))
        }
        
        val asignadoPor = SecurityUtils.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        val asignado = permisosService.asignarPermiso(id, request.codigoPermiso, asignadoPor)
        
        if (!asignado) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "No se pudo asignar el permiso. Verifica que el permiso existe y el usuario no lo tenga ya."))
        }
        
        val permisos = permisosService.obtenerPermisosUsuario(id)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Permiso asignado exitosamente",
            "permisos" to permisos
        ))
    }
    
    @DeleteMapping("/{id}/permisos/{codigoPermiso}")
    @Operation(
        summary = "Revocar Permiso de Usuario",
        description = "Revoca un permiso adicional de un usuario. Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun revocarPermiso(
        @PathVariable id: Int,
        @PathVariable codigoPermiso: String
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeAsignarPermisos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para revocar permisos"))
        }
        
        val revocado = permisosService.revocarPermiso(id, codigoPermiso)
        
        if (!revocado) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "No se pudo revocar el permiso. Solo se pueden revocar permisos adicionales, no los del rol base."))
        }
        
        val permisos = permisosService.obtenerPermisosUsuario(id)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Permiso revocado exitosamente",
            "permisos" to permisos
        ))
    }
    
    @GetMapping("/permisos/disponibles")
    @Operation(
        summary = "Listar Permisos Disponibles",
        description = "Lista todos los permisos disponibles en el sistema. Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listarPermisosDisponibles(): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeAsignarPermisos()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para ver permisos"))
        }
        
        val permisos = permisosService.obtenerTodosPermisos()
        val permisosPorCategoria = permisos.groupBy { it.categoria }
            .mapValues { (_, permisos) -> permisos.map { mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "descripcion" to it.descripcion
            ) } }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "permisos" to permisos.map { mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "descripcion" to it.descripcion,
                "categoria" to it.categoria
            ) },
            "permisosPorCategoria" to permisosPorCategoria,
            "total" to permisos.size
        ))
    }
}
