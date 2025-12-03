package com.siga.backend.repository

import com.siga.backend.entity.EstadoSuscripcion
import com.siga.backend.entity.Suscripcion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface SuscripcionRepository : JpaRepository<Suscripcion, Int> {
    @Query("SELECT s FROM Suscripcion s WHERE s.usuarioId = :usuarioId ORDER BY s.fechaCreacion DESC")
    fun findByUsuarioId(@Param("usuarioId") usuarioId: Int): List<Suscripcion>
    
    @Query("SELECT s FROM Suscripcion s WHERE s.usuarioId = :usuarioId AND s.estado = :estado AND (s.fechaFin IS NULL OR s.fechaFin >= :fechaActual) ORDER BY s.fechaCreacion DESC")
    fun findActiveByUsuarioId(@Param("usuarioId") usuarioId: Int, @Param("estado") estado: EstadoSuscripcion, @Param("fechaActual") fechaActual: LocalDate): List<Suscripcion>
    
    @Query("SELECT s FROM Suscripcion s WHERE s.usuarioId IN (SELECT u.id FROM UsuarioComercial u WHERE u.email = :email) AND s.estado = :estado AND (s.fechaFin IS NULL OR s.fechaFin >= :fechaActual)")
    fun findActiveByEmail(@Param("email") email: String, @Param("estado") estado: EstadoSuscripcion, @Param("fechaActual") fechaActual: LocalDate): List<Suscripcion>
}

