package com.siga.backend.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JWTService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.issuer:siga-backend}") private val issuer: String,
    @Value("\${jwt.access-token-expiry:86400000}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry:604800000}") private val refreshTokenExpiry: Long
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    
    fun generateAccessToken(userId: Int, email: String, rol: String? = null, usuarioComercialId: Int? = null, nombreEmpresa: String? = null): String {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("type", "access")
            .apply {
                if (rol != null) {
                    withClaim("rol", rol)
                }
                if (usuarioComercialId != null) {
                    withClaim("usuario_comercial_id", usuarioComercialId)
                }
                if (nombreEmpresa != null) {
                    withClaim("nombre_empresa", nombreEmpresa)
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
}

