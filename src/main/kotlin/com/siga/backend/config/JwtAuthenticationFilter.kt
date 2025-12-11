package com.siga.backend.config

import com.auth0.jwt.interfaces.DecodedJWT
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtVerifier: com.auth0.jwt.interfaces.JWTVerifier
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)
        
        if (token != null) {
            try {
                val decodedJWT: DecodedJWT = jwtVerifier.verify(token)
                val tokenType = decodedJWT.getClaim("type").asString()
                
                // Solo permitir access tokens (no refresh tokens)
                if (tokenType == "access") {
                    val userId = decodedJWT.subject
                    val email = decodedJWT.getClaim("email")?.asString()
                    val rol = decodedJWT.getClaim("rol")?.asString() // Puede ser null para usuarios comerciales
                    
                    // Crear autenticación
                    val authorities = if (rol != null) {
                        listOf(SimpleGrantedAuthority("ROLE_$rol"))
                    } else {
                        emptyList()
                    }
                    
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                    ).apply {
                        details = mapOf(
                            "email" to (email ?: ""),
                            "rol" to (rol ?: "")
                        )
                    }
                    
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
                // Token inválido, continuar sin autenticación
                SecurityContextHolder.clearContext()
            }
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}

