package com.siga.backend.utils

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    
    fun getUserId(): Int? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.name?.toIntOrNull()
    }
    
    fun getUserEmail(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        val details = authentication?.details as? Map<*, *>
        return details?.get("email") as? String
    }
    
    fun getUserRol(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        val details = authentication?.details as? Map<*, *>
        return details?.get("rol") as? String
    }
    
    fun hasRole(requiredRol: String): Boolean {
        val userRol = getUserRol()
        return userRol == requiredRol
    }
    
    fun isAdmin(): Boolean {
        return hasRole("ADMINISTRADOR")
    }
    
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }
}

