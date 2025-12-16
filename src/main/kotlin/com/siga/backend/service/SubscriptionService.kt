package com.siga.backend.service

import com.siga.backend.entity.EstadoSuscripcion
import com.siga.backend.repository.SuscripcionRepository
import com.siga.backend.repository.UsuarioComercialRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

@Service
class SubscriptionService(
    private val suscripcionRepository: SuscripcionRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository
) {
    
    fun hasActiveSubscription(email: String): Boolean {
        return try {
            val usuario = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
                ?: return false
                
            checkSubscriptionStatus(usuario)
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
        // Verificar si tiene trial activo (14 días)
        if (usuario.enTrial && usuario.fechaFinTrial != null) {
            val ahora = Instant.now()
            if (ahora.isBefore(usuario.fechaFinTrial)) {
                return true // Trial activo
            } else {
                // Trial expirado, desactivar
                val usuarioActualizado = usuario.copy(
                    enTrial = false,
                    fechaActualizacion = ahora
                )
                usuarioComercialRepository.save(usuarioActualizado)
            }
        }
        
        // Verificar suscripción activa
        val hoy = LocalDate.now()
        val suscripciones = suscripcionRepository.findActiveByEmail(
            usuario.email.lowercase(),
            EstadoSuscripcion.ACTIVA,
            hoy
        )
        
        return suscripciones.isNotEmpty()
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

