package com.siga.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

object ProductoTable : Table("siga_saas.PRODUCTOS") {
    
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 200)
    val descripcion = text("descripcion").nullable()
    val categoriaId = integer("categoria_id").nullable()
    val codigoBarras = varchar("codigo_barras", 50).nullable().uniqueIndex()
    val precioUnitario = decimal("precio_unitario", 10, 2).nullable()
    val activo = bool("activo").default(true)
    val fechaCreacion = timestamp("fecha_creacion").default(Instant.now())
    val fechaActualizacion = timestamp("fecha_actualizacion").default(Instant.now())
}
