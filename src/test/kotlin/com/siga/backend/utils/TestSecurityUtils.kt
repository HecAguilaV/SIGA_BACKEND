package com.siga.backend.utils

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

object TestSecurityUtils {
    
    fun setupSecurityContext(userId: Int, email: String, rol: String) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$rol"))
        // Configurar detalles adicionales para SecurityUtils (igual que JwtAuthenticationFilter)
        val details: Map<String, String> = mapOf(
            "email" to email,
            "rol" to rol
        )
        
        // Usar el método estático authenticated() para crear un token autenticado
        val authentication = UsernamePasswordAuthenticationToken.authenticated(
            userId.toString(),
            null,
            authorities
        )
        authentication.details = details
        
        SecurityContextHolder.getContext().authentication = authentication
    }
    
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }
}

