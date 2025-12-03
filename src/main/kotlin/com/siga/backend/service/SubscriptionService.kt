package com.siga.backend.service

import com.siga.backend.entity.EstadoSuscripcion
import com.siga.backend.repository.SuscripcionRepository
import com.siga.backend.repository.UsuarioComercialRepository
import org.springframework.stereotype.Service
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
            
            val hoy = LocalDate.now()
            val suscripciones = suscripcionRepository.findActiveByEmail(
                email.lowercase(),
                EstadoSuscripcion.ACTIVA,
                hoy
            )
            
            suscripciones.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}

