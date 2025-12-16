package com.siga.backend.controller

import com.siga.backend.entity.Rol
import com.siga.backend.entity.UsuarioSaas
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.LocalRepository
import com.siga.backend.service.JWTService
import com.siga.backend.service.PasswordService
import com.siga.backend.service.PermisosService
import java.time.Instant
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import com.siga.backend.utils.SecurityUtils

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
)

data class RegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val nombre: String,
    val apellido: String? = null,
    val rol: String = "OPERADOR"
)

data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserInfo? = null
)

data class UserInfo(
    val id: Int,
    val email: String,
    val nombre: String,
    val apellido: String?,
    val rol: String,
    val nombreEmpresa: String? = null,
    val localPorDefecto: LocalInfo? = null,
    val permisos: List<String> = emptyList()
)

data class LocalInfo(
    val id: Int,
    val nombre: String,
    val ciudad: String?
)

@RestController
@RequestMapping("/api/auth")
@Tag(name = "2. Autenticación", description = "Registro y login de usuarios operativos")
class AuthController(
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val localRepository: LocalRepository,
    private val passwordService: PasswordService,
    private val jwtService: JWTService,
    private val permisosService: PermisosService
) {
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar Sesión", description = "Autentica un usuario operativo (ADMINISTRADOR, OPERADOR, CAJERO) y obtiene tokens JWT. Auto-asigna empresa si no tiene.")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = usuarioSaasRepository.findByEmail(request.email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Credenciales inválidas"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Usuario inactivo"))
        }
        
        if (!passwordService.verifyPassword(request.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Credenciales inválidas"))
        }
        
        // AUTO-ASIGNAR EMPRESA si no tiene
        var usuarioActualizado = user
        var usuarioComercial = if (user.usuarioComercialId != null) {
            usuarioComercialRepository.findById(user.usuarioComercialId).orElse(null)
        } else {
            null
        }
        
        if (user.usuarioComercialId == null || usuarioComercial == null) {
            usuarioComercial = usuarioComercialRepository.findByEmail(user.email.lowercase()).orElse(null)
            if (usuarioComercial != null) {
                usuarioActualizado = user.copy(
                    usuarioComercialId = usuarioComercial.id,
                    fechaActualizacion = Instant.now()
                )
                usuarioSaasRepository.save(usuarioActualizado)
            }
        }
        
        // Obtener local por defecto (primer local activo de la empresa)
        val localPorDefecto = usuarioActualizado.usuarioComercialId?.let { comercialId ->
            localRepository.findByActivoTrueAndUsuarioComercialId(comercialId)
                .firstOrNull()
                ?.let { LocalInfo(id = it.id, nombre = it.nombre, ciudad = it.ciudad) }
        }
        
        val accessToken = jwtService.generateAccessToken(
            usuarioActualizado.id, 
            usuarioActualizado.email, 
            usuarioActualizado.rol.name,
            usuarioActualizado.usuarioComercialId,
            usuarioComercial?.nombreEmpresa
        )
        val refreshToken = jwtService.generateRefreshToken(usuarioActualizado.id)
        
        return ResponseEntity.ok(
            AuthResponse(
                success = true,
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = UserInfo(
                    id = usuarioActualizado.id,
                    email = usuarioActualizado.email,
                    nombre = usuarioActualizado.nombre,
                    apellido = usuarioActualizado.apellido,
                    rol = usuarioActualizado.rol.name,
                    nombreEmpresa = usuarioComercial?.nombreEmpresa,
                    localPorDefecto = localPorDefecto,
                    permisos = permisosService.obtenerPermisosUsuario(usuarioActualizado.id)
                )
            )
        )
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registrar Usuario", description = "Registra un nuevo usuario operativo. Roles: ADMINISTRADOR, OPERADOR, CAJERO")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val rol = try {
            Rol.valueOf(request.rol.uppercase())
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthResponse(success = false, message = "Rol inválido. Debe ser: ADMINISTRADOR, OPERADOR o CAJERO"))
        }
        
        if (usuarioSaasRepository.existsByEmail(request.email.lowercase())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthResponse(success = false, message = "El email ya está registrado"))
        }
        
        val passwordHash = passwordService.hashPassword(request.password)
        val newUser = UsuarioSaas(
            email = request.email.lowercase(),
            passwordHash = passwordHash,
            nombre = request.nombre,
            apellido = request.apellido,
            rol = rol
        )
        
        val savedUser = usuarioSaasRepository.save(newUser)
        
        val accessToken = jwtService.generateAccessToken(savedUser.id, savedUser.email, savedUser.rol.name)
        val refreshToken = jwtService.generateRefreshToken(savedUser.id)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponse(
                success = true,
                message = "Usuario registrado exitosamente",
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = UserInfo(
                    id = savedUser.id,
                    email = savedUser.email,
                    nombre = savedUser.nombre,
                    apellido = savedUser.apellido,
                    rol = savedUser.rol.name,
                    permisos = permisosService.obtenerPermisosUsuario(savedUser.id)
                )
            )
        )
    }
    
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val decodedJWT = jwtService.verifyToken(request.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Token inválido"))
        
        if (decodedJWT.getClaim("type").asString() != "refresh") {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Token de refresh inválido"))
        }
        
        val userId = decodedJWT.subject.toIntOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Token inválido"))
        
        val user = usuarioSaasRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Usuario no encontrado"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse(success = false, message = "Usuario inactivo"))
        }
        
        val newAccessToken = jwtService.generateAccessToken(user.id, user.email, user.rol.name)
        val newRefreshToken = jwtService.generateRefreshToken(user.id)
        
        return ResponseEntity.ok(
            AuthResponse(
                success = true,
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )
    }
    
    @GetMapping("/me")
    @Operation(
        summary = "Obtener Perfil Actual",
        description = "Obtiene la información del usuario autenticado, incluyendo nombre de empresa y local por defecto.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun obtenerPerfil(): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        val user = usuarioSaasRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Usuario inactivo"))
        }
        
        // Obtener información de la empresa
        val usuarioComercial = user.usuarioComercialId?.let {
            usuarioComercialRepository.findById(it).orElse(null)
        }
        
        // Obtener local por defecto (primer local activo de la empresa)
        val localPorDefecto = user.usuarioComercialId?.let { comercialId ->
            localRepository.findByActivoTrueAndUsuarioComercialId(comercialId)
                .firstOrNull()
                ?.let { LocalInfo(id = it.id, nombre = it.nombre, ciudad = it.ciudad) }
        }
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "user" to UserInfo(
                id = user.id,
                email = user.email,
                nombre = user.nombre,
                apellido = user.apellido,
                rol = user.rol.name,
                nombreEmpresa = usuarioComercial?.nombreEmpresa,
                localPorDefecto = localPorDefecto,
                permisos = permisosService.obtenerPermisosUsuario(user.id)
            )
        ))
    }
    
}

