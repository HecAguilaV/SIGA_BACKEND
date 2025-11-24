package com.siga.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

object PlanTable : Table("siga_comercial.PLANES") {
    
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100).uniqueIndex()
    val descripcion = text("descripcion").nullable()
    val precioMensual = decimal("precio_mensual", 10, 2).check { it greaterEq BigDecimal.ZERO }
    val precioAnual = decimal("precio_anual", 10, 2).nullable().check { it greaterEq BigDecimal.ZERO }
    val limiteBodegas = integer("limite_bodegas").default(1).check { it greater 0 }
    val limiteUsuarios = integer("limite_usuarios").default(1).check { it greater 0 }
    val limiteProductos = integer("limite_productos").nullable()
    val caracteristicas = text("caracteristicas").nullable() // JSONB en PostgreSQL (almacenado como texto)
    val activo = bool("activo").default(true)
    val orden = integer("orden").default(0)
    val fechaCreacion = timestamp("fecha_creacion").default(Instant.now())
}
