package com.siga.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.siga.backend.utils.EnvLoader
import java.util.*

object JWTService {
    private val secret = EnvLoader.getEnv("JWT_SECRET") ?: "default_secret_change_in_production"
    private val algorithm = Algorithm.HMAC256(secret)
    private val issuer = "siga-backend"
    private val accessTokenExpiry = 24 * 60 * 60 * 1000L // 24 horas
    private val refreshTokenExpiry = 7 * 24 * 60 * 60 * 1000L // 7 días
    
    fun generateAccessToken(userId: Int, email: String, rol: String? = null): String {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("type", "access")
            .apply {
                if (rol != null) {
                    withClaim("rol", rol)
                }
            }
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
            .withIssuedAt(Date())
            .sign(algorithm)
    }
    
    fun generateRefreshToken(userId: Int): String {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .withIssuedAt(Date())
            .sign(algorithm)
    }
    
    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getUserIdFromToken(token: String): Int? {
        return verifyToken(token)?.subject?.toIntOrNull()
    }
    
    fun getEmailFromToken(token: String): String? {
        return verifyToken(token)?.getClaim("email")?.asString()
    }
    
    fun getRolFromToken(token: String): String? {
        return verifyToken(token)?.getClaim("rol")?.asString()
    }
    
    fun isRefreshToken(token: String): Boolean {
        return verifyToken(token)?.getClaim("type")?.asString() == "refresh"
    }
}