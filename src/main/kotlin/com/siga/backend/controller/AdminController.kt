package com.siga.backend.controller

import com.siga.backend.repository.UsuarioSaasRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
@Tag(name = "5. Administración", description = "Endpoints administrativos para gestión de usuarios y configuración del sistema")
class AdminController(
    private val usuarioSaasRepository: UsuarioSaasRepository
) {
    
    @GetMapping("/users")
    @Operation(
        summary = "Listar Usuarios",
        description = "Lista todos los usuarios operativos del sistema. Muestra ID, email, nombre, apellido, rol y estado (sin contraseñas). Útil para verificar qué usuarios existen en la base de datos.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun listUsers(): ResponseEntity<Map<String, Any>> {
        val users = usuarioSaasRepository.findAll().map { user ->
            mapOf(
                "id" to user.id,
                "email" to user.email,
                "nombre" to user.nombre,
                "apellido" to user.apellido,
                "rol" to user.rol.name,
                "activo" to user.activo
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "total" to users.size,
            "usuarios" to users
        ))
    }
}

