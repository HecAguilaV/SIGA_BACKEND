package com.siga.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.siga.backend.utils.EnvLoader
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.http.*

/**
 * Configura el middleware de autenticación JWT
 * 
 * Este middleware intercepta las peticiones y valida el token JWT
 * antes de que lleguen a los endpoints protegidos.
 */
fun Application.configureJWTAuth() {
    val secret = EnvLoader.getEnv("JWT_SECRET") ?: "default_secret_change_in_production"
    val issuer = "siga-backend"
    val algorithm = Algorithm.HMAC256(secret)
    
    install(Authentication) {
        jwt("jwt") {
            // Configurar el verifier (valida firma, expiración, issuer)
            realm = issuer
            verifier(
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
            )
            
            // Validar el token y verificar que sea un access token
            validate { credential ->
                val tokenType = credential.payload.getClaim("type").asString()
                
                // Solo permitir access tokens (no refresh tokens)
                if (tokenType == "access") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            
            // Manejar errores de autenticación
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "success" to false,
                        "message" to "Token inválido o expirado. Por favor, inicia sesión nuevamente."
                    )
                )
            }
        }
    }
}

/**
 * Obtiene el ID del usuario autenticado desde el JWT
 */
fun ApplicationCall.getUserId(): Int? {
    val principal = this.principal<JWTPrincipal>()
    return principal?.payload?.subject?.toIntOrNull()
}

/**
 * Obtiene el email del usuario autenticado desde el JWT
 */
fun ApplicationCall.getUserEmail(): String? {
    val principal = this.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("email")?.asString()
}

/**
 * Obtiene el rol del usuario autenticado desde el JWT
 */
fun ApplicationCall.getUserRol(): String? {
    val principal = this.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("rol")?.asString()
}

/**
 * Verifica si el usuario tiene un rol específico
 */
fun ApplicationCall.hasRole(requiredRol: String): Boolean {
    val userRol = getUserRol()
    return userRol == requiredRol
}

/**
 * Verifica si el usuario tiene uno de los roles permitidos
 */
fun ApplicationCall.hasAnyRole(vararg allowedRoles: String): Boolean {
    val userRol = getUserRol()
    return allowedRoles.contains(userRol)
}

/**
 * Verifica si el usuario es ADMINISTRADOR
 */
fun ApplicationCall.isAdmin(): Boolean {
    return hasRole("ADMINISTRADOR")
}

