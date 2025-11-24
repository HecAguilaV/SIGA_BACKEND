package com.siga.backend.services

import com.siga.backend.models.SuscripcionTable
import com.siga.backend.models.UsuarioComercialTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

/**
 * Servicio para validar suscripciones de usuarios
 */
object SubscriptionService {
    
    /**
     * Verifica si un usuario tiene una suscripción activa
     * 
     * @param email Email del usuario (debe existir en siga_comercial.USUARIOS)
     * @return true si tiene suscripción activa y no vencida, false en caso contrario
     */
    fun hasActiveSubscription(email: String): Boolean {
        return try {
            transaction {
                // Buscar usuario en siga_comercial por email
                val usuarioComercial = UsuarioComercialTable.select {
                    UsuarioComercialTable.email eq email.lowercase()
                }.firstOrNull()
                
                if (usuarioComercial == null) {
                    return@transaction false
                }
                
                val usuarioComercialId = usuarioComercial[UsuarioComercialTable.id]
                
                // Buscar suscripción activa
                val suscripcion = SuscripcionTable.select {
                    (SuscripcionTable.usuarioId eq usuarioComercialId) and
                    (SuscripcionTable.estado eq "ACTIVA")
                }.firstOrNull()
                
                if (suscripcion == null) {
                    return@transaction false
                }
                
                // Verificar que no esté vencida
                val fechaFin = suscripcion[SuscripcionTable.fechaFin]
                val hoy = LocalDate.now()
                
                // Si no tiene fecha_fin, se considera activa
                // Si tiene fecha_fin, debe ser mayor o igual a hoy
                fechaFin == null || !fechaFin.isBefore(hoy)
            }
        } catch (e: Exception) {
            // En caso de error, retornar false por seguridad
            false
        }
    }
    
    /**
     * Obtiene información de la suscripción del usuario
     * 
     * @param email Email del usuario
     * @return Información de la suscripción o null si no existe
     */
    data class SubscriptionInfo(
        val planId: Int,
        val estado: String,
        val fechaInicio: LocalDate,
        val fechaFin: LocalDate?,
        val periodo: String
    )
    
    fun getSubscriptionInfo(email: String): SubscriptionInfo? {
        return try {
            transaction {
                val usuarioComercial = UsuarioComercialTable.select {
                    UsuarioComercialTable.email eq email.lowercase()
                }.firstOrNull() ?: return@transaction null
                
                val suscripcion = SuscripcionTable.select {
                    SuscripcionTable.usuarioId eq usuarioComercial[UsuarioComercialTable.id]
                }.orderBy(SuscripcionTable.fechaCreacion, SortOrder.DESC).firstOrNull()
                    ?: return@transaction null
                
                SubscriptionInfo(
                    planId = suscripcion[SuscripcionTable.planId],
                    estado = suscripcion[SuscripcionTable.estado],
                    fechaInicio = suscripcion[SuscripcionTable.fechaInicio],
                    fechaFin = suscripcion[SuscripcionTable.fechaFin],
                    periodo = suscripcion[SuscripcionTable.periodo]
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

