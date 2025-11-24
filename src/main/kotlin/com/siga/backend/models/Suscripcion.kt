package com.siga.backend.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
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

object SuscripcionTable : Table("siga_comercial.SUSCRIPCIONES") {
    
    val id = integer("id").autoIncrement()
    val usuarioId = integer("usuario_id").references(UsuarioComercialTable.id, onDelete = ReferenceOption.CASCADE)
    val planId = integer("plan_id").references(PlanTable.id)
    val fechaInicio = date("fecha_inicio")
    val fechaFin = date("fecha_fin").nullable()
    val estado = varchar("estado", 20).default("ACTIVA")
    val periodo = varchar("periodo", 20).default("MENSUAL")
    val fechaCreacion = timestamp("fecha_creacion").default(Instant.now())
    val fechaActualizacion = timestamp("fecha_actualizacion").default(Instant.now())
}
