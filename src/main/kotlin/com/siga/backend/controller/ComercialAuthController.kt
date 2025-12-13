package com.siga.backend.controller

import com.siga.backend.entity.UsuarioComercial
import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.service.JWTService
import com.siga.backend.service.PasswordService
import com.siga.backend.service.SubscriptionService
import com.siga.backend.utils.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class ComercialLoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
)

data class ComercialRegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val nombre: String,
    val apellido: String? = null,
    val rut: String? = null,
    val telefono: String? = null,
    val nombreEmpresa: String? = null
)

data class ComercialRefreshTokenRequest(
    @field:NotBlank val refreshToken: String
)

data class ResetPasswordRequest(
    @field:NotBlank @field:Email val email: String
)

data class ChangePasswordRequest(
    @field:NotBlank val token: String,
    @field:NotBlank val newPassword: String
)

data class UpdateEmailRequest(
    @field:NotBlank @field:Email val newEmail: String,
    @field:NotBlank val password: String  // Confirmar con contraseña actual
)

data class ComercialAuthResponse(
    val success: Boolean,
    val message: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: ComercialUserInfo? = null
)

data class ComercialUserInfo(
    val id: Int,
    val email: String,
    val nombre: String,
    val apellido: String?,
    val rut: String?,
    val telefono: String?,
    val nombreEmpresa: String? = null
)

@RestController
@RequestMapping("/api/comercial/auth")
@Tag(name = "1. Público - Sin Autenticación", description = "Endpoints públicos de autenticación comercial")
class ComercialAuthController(
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val passwordService: PasswordService,
    private val jwtService: JWTService,
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val subscriptionService: SubscriptionService
) {
    
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar Sesión Comercial",
        description = "Autentica un usuario comercial (cliente) y obtiene tokens JWT. NO requiere autenticación previa."
    )
    fun login(@Valid @RequestBody request: ComercialLoginRequest): ResponseEntity<ComercialAuthResponse> {
        val user = usuarioComercialRepository.findByEmail(request.email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Credenciales inválidas"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Usuario inactivo"))
        }
        
        if (!passwordService.verifyPassword(request.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Credenciales inválidas"))
        }
        
        // Para usuarios comerciales no hay rol, se genera token sin rol
        val accessToken = jwtService.generateAccessToken(user.id, user.email, null)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        
        return ResponseEntity.ok(
            ComercialAuthResponse(
                success = true,
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = ComercialUserInfo(
                    id = user.id,
                    email = user.email,
                    nombre = user.nombre,
                    apellido = user.apellido,
                    rut = user.rut,
                    telefono = user.telefono
                )
            )
        )
    }
    
    @PostMapping("/register")
    @Operation(
        summary = "Registrar Usuario Comercial",
        description = "Registra un nuevo usuario comercial (cliente). NO requiere autenticación previa."
    )
    fun register(@Valid @RequestBody request: ComercialRegisterRequest): ResponseEntity<ComercialAuthResponse> {
        if (usuarioComercialRepository.existsByEmail(request.email.lowercase())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ComercialAuthResponse(success = false, message = "El email ya está registrado"))
        }
        
        val passwordHash = passwordService.hashPassword(request.password)
        val newUser = UsuarioComercial(
            email = request.email.lowercase(),
            passwordHash = passwordHash,
            nombre = request.nombre,
            apellido = request.apellido,
            rut = request.rut,
            telefono = request.telefono,
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )
        
        val savedUser = usuarioComercialRepository.save(newUser)
        
        // Para usuarios comerciales no hay rol, se genera token sin rol
        val accessToken = jwtService.generateAccessToken(savedUser.id, savedUser.email, null)
        val refreshToken = jwtService.generateRefreshToken(savedUser.id)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ComercialAuthResponse(
                success = true,
                message = "Usuario registrado exitosamente",
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = ComercialUserInfo(
                    id = savedUser.id,
                    email = savedUser.email,
                    nombre = savedUser.nombre,
                    apellido = savedUser.apellido,
                    rut = savedUser.rut,
                    telefono = savedUser.telefono,
                    nombreEmpresa = savedUser.nombreEmpresa
                )
            )
        )
    }
    
    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar Token Comercial",
        description = "Renueva el token de acceso usando un refresh token válido. NO requiere autenticación previa."
    )
    fun refresh(@Valid @RequestBody request: ComercialRefreshTokenRequest): ResponseEntity<ComercialAuthResponse> {
        val decodedJWT = jwtService.verifyToken(request.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Token inválido"))
        
        if (decodedJWT.getClaim("type").asString() != "refresh") {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Token de refresh inválido"))
        }
        
        val userId = decodedJWT.subject.toIntOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Token inválido"))
        
        val user = usuarioComercialRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Usuario no encontrado"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ComercialAuthResponse(success = false, message = "Usuario inactivo"))
        }
        
        // Para usuarios comerciales no hay rol, se genera token sin rol
        val newAccessToken = jwtService.generateAccessToken(user.id, user.email, null)
        val newRefreshToken = jwtService.generateRefreshToken(user.id)
        
        return ResponseEntity.ok(
            ComercialAuthResponse(
                success = true,
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )
    }
    
    @PostMapping("/obtener-token-operativo")
    @Operation(
        summary = "Obtener Token Operativo para WebApp",
        description = "Intercambia token comercial por token operativo para acceder a WebApp. Requiere autenticación comercial y suscripción activa.",
        security = [io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")]
    )
    fun obtenerTokenOperativo(): ResponseEntity<Map<String, Any>> {
        val email = SecurityUtils.getUserEmail()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        // Verificar que el usuario comercial existe
        val usuarioComercial = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario comercial no encontrado"))
        
        // Verificar suscripción activa
        if (!subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(mapOf("success" to false, "message" to "Se requiere una suscripción activa para acceder a WebApp"))
        }
        
        // Buscar usuario operativo (debe existir porque se crea automáticamente al adquirir suscripción)
        val usuarioOperativo = usuarioSaasRepository.findByEmail(email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf(
                    "success" to false, 
                    "message" to "Usuario operativo no encontrado. Por favor, contacta al administrador o intenta crear una nueva suscripción."
                ))
        
        if (!usuarioOperativo.activo) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "Usuario operativo inactivo"))
        }
        
        // Generar token operativo
        val accessToken = jwtService.generateAccessToken(
            usuarioOperativo.id,
            usuarioOperativo.email,
            usuarioOperativo.rol.name
        )
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "accessToken" to accessToken,
            "user" to mapOf(
                "id" to usuarioOperativo.id,
                "email" to usuarioOperativo.email,
                "nombre" to usuarioOperativo.nombre,
                "apellido" to usuarioOperativo.apellido,
                "rol" to usuarioOperativo.rol.name
            )
        ))
    }
    
    @PostMapping("/reset-password")
    @Operation(
        summary = "Solicitar Reset de Contraseña",
        description = "Solicita un token para resetear la contraseña. Envía email al usuario (por ahora solo devuelve token en respuesta para testing)."
    )
    fun solicitarResetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<Map<String, Any>> {
        val user = usuarioComercialRepository.findByEmail(request.email.lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Email no encontrado"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "Usuario inactivo"))
        }
        
        // Generar token de reset (válido por 1 hora)
        val resetToken = jwtService.generateAccessToken(user.id, user.email, "RESET")
        
        // TODO: En producción, enviar email con el token
        // Por ahora, devolvemos el token en la respuesta (solo para testing)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Token de reset generado. En producción se enviará por email.",
            "resetToken" to resetToken, // ⚠️ Solo para testing, quitar en producción
            "expiresIn" to 3600 // 1 hora
        ))
    }
    
    @PostMapping("/change-password")
    @Operation(
        summary = "Cambiar Contraseña con Token",
        description = "Cambia la contraseña usando el token de reset recibido por email."
    )
    fun cambiarPassword(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<Map<String, Any>> {
        val decodedJWT = jwtService.verifyToken(request.token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido o expirado"))
        
        val userId = decodedJWT.subject.toIntOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido"))
        
        val user = usuarioComercialRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        if (!user.activo) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("success" to false, "message" to "Usuario inactivo"))
        }
        
        // Actualizar contraseña
        val newPasswordHash = passwordService.hashPassword(request.newPassword)
        val updatedUser = user.copy(
            passwordHash = newPasswordHash,
            fechaActualizacion = Instant.now()
        )
        
        usuarioComercialRepository.save(updatedUser)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Contraseña actualizada exitosamente"
        ))
    }
    
    @PutMapping("/update-email")
    @Operation(
        summary = "Actualizar Email",
        description = "Actualiza el email del usuario autenticado. Requiere confirmar con contraseña actual. Requiere autenticación.",
        security = [io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")]
    )
    fun actualizarEmail(@Valid @RequestBody request: UpdateEmailRequest): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "No autenticado"))
        
        val user = usuarioComercialRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Usuario no encontrado"))
        
        // Verificar contraseña actual
        if (!passwordService.verifyPassword(request.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Contraseña incorrecta"))
        }
        
        // Verificar que el nuevo email no esté en uso
        val nuevoEmailLower = request.newEmail.lowercase()
        if (nuevoEmailLower == user.email.lowercase()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "El nuevo email es igual al actual"))
        }
        
        if (usuarioComercialRepository.existsByEmail(nuevoEmailLower)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "El email ya está en uso por otro usuario"))
        }
        
        // Actualizar email en usuario comercial
        val updatedUser = user.copy(
            email = nuevoEmailLower,
            fechaActualizacion = Instant.now()
        )
        usuarioComercialRepository.save(updatedUser)
        
        // Actualizar email en usuario operativo si existe (para mantener sincronización)
        val usuarioOperativo = usuarioSaasRepository.findByEmail(user.email.lowercase()).orElse(null)
        if (usuarioOperativo != null) {
            val updatedOperativo = usuarioOperativo.copy(
                email = nuevoEmailLower,
                fechaActualizacion = Instant.now()
            )
            usuarioSaasRepository.save(updatedOperativo)
        }
        
        // Generar nuevos tokens con el email actualizado
        val newAccessToken = jwtService.generateAccessToken(updatedUser.id, updatedUser.email, null)
        val newRefreshToken = jwtService.generateRefreshToken(updatedUser.id)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Email actualizado exitosamente",
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken,
            "user" to ComercialUserInfo(
                id = updatedUser.id,
                email = updatedUser.email,
                nombre = updatedUser.nombre,
                apellido = updatedUser.apellido,
                rut = updatedUser.rut,
                telefono = updatedUser.telefono,
                nombreEmpresa = updatedUser.nombreEmpresa
            )
        ))
    }
}
