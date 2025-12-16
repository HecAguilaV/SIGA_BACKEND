package com.siga.backend.controller

import com.siga.backend.entity.Rol
import com.siga.backend.entity.UsuarioSaas
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.repository.UsuarioComercialRepository
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
    val activo: Boolean? = null,
    val rol: String? = null
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

data class AsignarEmpresaRequest(
    val usuarioComercialId: Int? = null,
    val emailComercial: String? = null  // Opcional: buscar por email si no se proporciona ID
)

@RestController
@RequestMapping("/api/saas/usuarios")
@Tag(name = "4. Gesti?n Operativa", description = "Gesti?n de usuarios operativos y permisos. Requiere autenticaci?n + suscripci?n activa")
class UsuariosController(
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val permisosService: PermisosService,
    private val passwordService: PasswordService
) {
    
    @GetMapping
    @Operation(
        summary = "Listar Usuarios Operativos",
        description = "Lista todos los usuarios operativos. Solo administradores. Requiere autenticaci?n y suscripci?n activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listarUsuarios(): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para ver usuarios"))
        }
        
        // Obtener usuario operativo actual
        val userId = SecurityUtils.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        val usuarioActual = usuarioSaasRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        // Filtrar usuarios por empresa (usuario_comercial_id)
        // Solo mostrar usuarios de la misma empresa que el usuario actual
        val usuarios = if (usuarioActual.usuarioComercialId != null) {
            // Usuario tiene empresa asignada, filtrar por empresa
            usuarioSaasRepository.findByUsuarioComercialId(usuarioActual.usuarioComercialId)
        } else {
            // Usuario legacy sin empresa, buscar por email en usuarios comerciales
            val email = SecurityUtils.getUserEmail()
            val usuarioComercial = email?.let { 
                usuarioComercialRepository.findByEmail(it.lowercase()).orElse(null)
            }
            
            if (usuarioComercial != null) {
                // Encontr? usuario comercial, filtrar por su ID
                usuarioSaasRepository.findByUsuarioComercialId(usuarioComercial.id)
            } else {
                // No encontr? usuario comercial, retornar solo el usuario actual (legacy)
                listOf(usuarioActual)
            }
        }
        
        val usuariosResponse = usuarios.map { usuario ->
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
            "usuarios" to usuariosResponse,
            "total" to usuariosResponse.size
        ))
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener Usuario",
        description = "Obtiene informaci?n de un usuario operativo espec?fico. Solo administradores.",
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
        description = "Crea un nuevo usuario operativo. Solo administradores. Requiere autenticaci?n y suscripci?n activa.",
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
                .body(mapOf("success" to false, "message" to "Rol inv?lido. Debe ser: ADMINISTRADOR, OPERADOR o CAJERO"))
        }
        
        if (usuarioSaasRepository.existsByEmail(request.email.lowercase())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "El email ya est? registrado"))
        }
        
        // Obtener usuario actual para asignar la misma empresa
        val userId = SecurityUtils.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        val usuarioActual = usuarioSaasRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario actual no encontrado"))
        
        // Obtener usuario_comercial_id del usuario actual (o buscarlo por email si es legacy)
        val usuarioComercialId = usuarioActual.usuarioComercialId ?: run {
            val email = SecurityUtils.getUserEmail()
            email?.let { 
                usuarioComercialRepository.findByEmail(it.lowercase()).orElse(null)?.id
            }
        }
        
        val passwordHash = passwordService.hashPassword(request.password)
        val nuevoUsuario = UsuarioSaas(
            email = request.email.lowercase(),
            passwordHash = passwordHash,
            nombre = request.nombre,
            apellido = request.apellido,
            rol = rol,
            usuarioComercialId = usuarioComercialId, // Asignar la misma empresa que el usuario actual
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
        description = "Actualiza informaci?n de un usuario operativo. Solo administradores.",
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
        
        // Verificar que el usuario pertenece a la empresa del usuario actual
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && usuario.usuarioComercialId != null && usuario.usuarioComercialId != usuarioComercialId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes acceso a este usuario"))
        }
        
        // Parsear nuevo rol si se env?a
        val nuevoRol = request.rol?.let { 
            try { Rol.valueOf(it.uppercase()) } catch (e: Exception) { null } 
        } ?: usuario.rol

        val usuarioActualizado = usuario.copy(
            nombre = request.nombre ?: usuario.nombre,
            apellido = request.apellido ?: usuario.apellido,
            activo = request.activo ?: usuario.activo,
            rol = nuevoRol,
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
    
    @PutMapping("/{id}/empresa")
    @Operation(
        summary = "Asignar Empresa a Usuario",
        description = "Asigna un usuario_comercial_id a un usuario operativo. Puede usar usuarioComercialId o emailComercial. Solo administradores. URGENTE: Usar para solucionar 'no se determinó la empresa'.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun asignarEmpresa(
        @PathVariable id: Int,
        @RequestBody request: AsignarEmpresaRequest
    ): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso para asignar empresa"))
        }
        
        val usuario = usuarioSaasRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        // Determinar usuario_comercial_id
        val usuarioComercialId = when {
            request.usuarioComercialId != null -> {
                // Verificar que existe
                val comercial = usuarioComercialRepository.findById(request.usuarioComercialId).orElse(null)
                    ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(mapOf("success" to false, "message" to "Usuario comercial no encontrado"))
                request.usuarioComercialId
            }
            request.emailComercial != null -> {
                // Buscar por email
                val comercial = usuarioComercialRepository.findByEmail(request.emailComercial.lowercase()).orElse(null)
                    ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(mapOf("success" to false, "message" to "Usuario comercial no encontrado con email: ${request.emailComercial}"))
                comercial.id
            }
            else -> {
                // Auto-asignar: buscar por email del usuario operativo
                val comercial = usuarioComercialRepository.findByEmail(usuario.email.lowercase()).orElse(null)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(mapOf(
                            "success" to false, 
                            "message" to "No se pudo determinar la empresa. Proporciona usuarioComercialId o emailComercial.",
                            "sugerencia" to "Usa el email del usuario comercial o su ID"
                        ))
                comercial.id
            }
        }
        
        val usuarioActualizado = usuario.copy(
            usuarioComercialId = usuarioComercialId,
            fechaActualizacion = Instant.now()
        )
        usuarioSaasRepository.save(usuarioActualizado)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Empresa asignada exitosamente",
            "usuario" to mapOf(
                "id" to usuarioActualizado.id,
                "email" to usuarioActualizado.email,
                "usuarioComercialId" to usuarioActualizado.usuarioComercialId
            )
        ))
    }
    
    @GetMapping("/sin-empresa")
    @Operation(
        summary = "Listar Usuarios Sin Empresa",
        description = "Lista usuarios operativos que no tienen usuario_comercial_id asignado. Solo administradores.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listarUsuariosSinEmpresa(): ResponseEntity<Map<String, Any>> {
        if (!SecurityUtils.puedeCrearUsuarios()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "No tienes permiso"))
        }
        
        val usuarios = usuarioSaasRepository.findAll()
            .filter { it.usuarioComercialId == null }
            .map { usuario ->
                // Buscar si existe usuario comercial con mismo email
                val comercial = usuarioComercialRepository.findByEmail(usuario.email.lowercase()).orElse(null)
                mapOf(
                    "id" to usuario.id,
                    "email" to usuario.email,
                    "nombre" to usuario.nombre,
                    "rol" to usuario.rol.toString(),
                    "usuarioComercialId" to null,
                    "usuarioComercialEncontrado" to (comercial?.let { 
                        mapOf("id" to it.id, "email" to it.email, "nombre" to it.nombre)
                    })
                )
            }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "usuarios" to usuarios,
            "total" to usuarios.size
        ))
    }
}
