package com.siga.backend.api.auth

import com.siga.backend.models.Rol
import com.siga.backend.models.UsuarioSaasTable
import com.siga.backend.services.JWTService
import com.siga.backend.services.PasswordService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val apellido: String? = null,
    val rol: String = "OPERADOR"
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserInfo? = null
)

@Serializable
data class UserInfo(
    val id: Int,
    val email: String,
    val nombre: String,
    val apellido: String?,
    val rol: String
)

fun Application.configureAuthRoutes() {
    routing {
        route("/api/auth") {
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    
                    val user = transaction {
                        UsuarioSaasTable.select {
                            (UsuarioSaasTable.email eq request.email.lowercase()) and
                            (UsuarioSaasTable.activo eq true)
                        }.firstOrNull()
                    }
                    
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            AuthResponse(success = false, message = "Credenciales inválidas")
                        )
                        return@post
                    }
                    
                    val passwordHash = user[UsuarioSaasTable.passwordHash]
                    if (!PasswordService.verifyPassword(request.password, passwordHash)) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            AuthResponse(success = false, message = "Credenciales inválidas")
                        )
                        return@post
                    }
                    
                    val userId = user[UsuarioSaasTable.id]
                    val rol = user[UsuarioSaasTable.rol]
                    
                    val accessToken = JWTService.generateAccessToken(userId, request.email.lowercase(), rol)
                    val refreshToken = JWTService.generateRefreshToken(userId)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(
                            success = true,
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            user = UserInfo(
                                id = userId,
                                email = request.email.lowercase(),
                                nombre = user[UsuarioSaasTable.nombre],
                                apellido = user[UsuarioSaasTable.apellido],
                                rol = rol
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        AuthResponse(success = false, message = "Error al procesar la solicitud: ${e.message}")
                    )
                }
            }
            
            post("/register") {
                try {
                    val request = call.receive<RegisterRequest>()
                    
                    // Validar rol
                    val rol = try {
                        Rol.valueOf(request.rol.uppercase())
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            AuthResponse(success = false, message = "Rol inválido. Debe ser: ADMINISTRADOR, OPERADOR o CAJERO")
                        )
                        return@post
                    }
                    
                    // Verificar si el usuario ya existe
                    val existingUser = transaction {
                        UsuarioSaasTable.select {
                            UsuarioSaasTable.email eq request.email.lowercase()
                        }.firstOrNull()
                    }
                    
                    if (existingUser != null) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            AuthResponse(success = false, message = "El email ya está registrado")
                        )
                        return@post
                    }
                    
                    // Crear nuevo usuario
                    val passwordHash = PasswordService.hashPassword(request.password)
                    val userId = transaction {
                        UsuarioSaasTable.insert {
                            it[UsuarioSaasTable.email] = request.email.lowercase()
                            it[UsuarioSaasTable.passwordHash] = passwordHash
                            it[UsuarioSaasTable.nombre] = request.nombre
                            it[UsuarioSaasTable.apellido] = request.apellido
                            it[UsuarioSaasTable.rol] = rol.name
                            it[UsuarioSaasTable.activo] = true
                        }
                        // Obtener el id del usuario recién creado
                        UsuarioSaasTable.select {
                            UsuarioSaasTable.email eq request.email.lowercase()
                        }.first()[UsuarioSaasTable.id]
                    }
                    
                    val accessToken = JWTService.generateAccessToken(userId, request.email.lowercase(), rol.name)
                    val refreshToken = JWTService.generateRefreshToken(userId)
                    
                    call.respond(
                        HttpStatusCode.Created,
                        AuthResponse(
                            success = true,
                            message = "Usuario registrado exitosamente",
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            user = UserInfo(
                                id = userId,
                                email = request.email.lowercase(),
                                nombre = request.nombre,
                                apellido = request.apellido,
                                rol = rol.name
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        AuthResponse(success = false, message = "Error al registrar usuario: ${e.message}")
                    )
                }
            }
            
            post("/refresh") {
                try {
                    val request = call.receive<RefreshTokenRequest>()
                    
                    if (!JWTService.isRefreshToken(request.refreshToken)) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            AuthResponse(success = false, message = "Token de refresh inválido")
                        )
                        return@post
                    }
                    
                    val userId = JWTService.getUserIdFromToken(request.refreshToken)
                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            AuthResponse(success = false, message = "Token inválido")
                        )
                        return@post
                    }
                    
                    // Verificar que el usuario existe y está activo
                    val user = transaction {
                        UsuarioSaasTable.select {
                            (UsuarioSaasTable.id eq userId) and
                            (UsuarioSaasTable.activo eq true)
                        }.firstOrNull()
                    }
                    
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            AuthResponse(success = false, message = "Usuario no encontrado o inactivo")
                        )
                        return@post
                    }
                    
                    val email = user[UsuarioSaasTable.email]
                    val rol = user[UsuarioSaasTable.rol]
                    
                    val newAccessToken = JWTService.generateAccessToken(userId, email, rol)
                    val newRefreshToken = JWTService.generateRefreshToken(userId)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(
                            success = true,
                            accessToken = newAccessToken,
                            refreshToken = newRefreshToken
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        AuthResponse(success = false, message = "Error al refrescar token: ${e.message}")
                    )
                }
            }
        }
    }
}