package com.siga.backend.service

import com.siga.backend.entity.EstadoSuscripcion
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.repository.SuscripcionRepository
import com.siga.backend.repository.UsuarioComercialRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

@Service
class SubscriptionService(
    private val suscripcionRepository: SuscripcionRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val usuarioSaasRepository: UsuarioSaasRepository
) {
    
    fun hasActiveSubscription(email: String): Boolean {
        return try {
            // 1. Caso: Es el Dueño (Usuario Comercial)
            val usuarioComercial = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
            if (usuarioComercial != null) {
                return checkSubscriptionStatus(usuarioComercial)
            }
            
            // 2. Caso: Es un Operador (Empleado)
            val usuarioSaas = usuarioSaasRepository.findByEmail(email.lowercase()).orElse(null)
            if (usuarioSaas != null && usuarioSaas.usuarioComercialId != null) {
                // Hereda la suscripción del Jefe
                return hasActiveSubscription(usuarioSaas.usuarioComercialId)
            }
            
            return false
        } catch (e: Exception) {
            false
        }
    }

    fun hasActiveSubscription(usuarioComercialId: Int): Boolean {
        return try {
            val usuario = usuarioComercialRepository.findById(usuarioComercialId).orElse(null)
                ?: return false
                
            checkSubscriptionStatus(usuario)
        } catch (e: Exception) {
            false
        }
    }

    private fun checkSubscriptionStatus(usuario: com.siga.backend.entity.UsuarioComercial): Boolean {
        // 1. Revisar si tiene Trial activo
        if (usuario.enTrial && usuario.fechaFinTrial != null && Instant.now().isBefore(usuario.fechaFinTrial)) {
            return true
        }
        
        // 2. Revisar si tiene Suscripción Activa en DB
        val activeSubscriptions = suscripcionRepository.findActiveByUsuarioId(
            usuario.id, 
            EstadoSuscripcion.ACTIVA, 
            LocalDate.now()
        )
        
        return activeSubscriptions.isNotEmpty()
    }
    
    fun tieneTrialActivo(email: String): Boolean {
        return try {
            val usuario = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
                ?: return false
            
            if (!usuario.enTrial || usuario.fechaFinTrial == null) {
                return false
            }
            
            Instant.now().isBefore(usuario.fechaFinTrial)
        } catch (e: Exception) {
            false
        }
    }
}

