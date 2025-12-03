package com.siga.backend.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

enum class EstadoSuscripcion {
    ACTIVA,
    SUSPENDIDA,
    CANCELADA,
    VENCIDA
}

enum class PeriodoSuscripcion {
    MENSUAL,
    ANUAL
}

@Entity
@Table(name = "SUSCRIPCIONES", schema = "siga_comercial")
data class Suscripcion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(name = "usuario_id", nullable = false)
    val usuarioId: Int,
    
    @Column(name = "plan_id", nullable = false)
    val planId: Int,
    
    @Column(name = "fecha_inicio", nullable = false)
    val fechaInicio: LocalDate,
    
    @Column(name = "fecha_fin")
    val fechaFin: LocalDate? = null,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val estado: EstadoSuscripcion = EstadoSuscripcion.ACTIVA,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val periodo: PeriodoSuscripcion = PeriodoSuscripcion.MENSUAL,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: Instant = Instant.now(),
    
    @Column(name = "fecha_actualizacion", nullable = false)
    val fechaActualizacion: Instant = Instant.now()
)

