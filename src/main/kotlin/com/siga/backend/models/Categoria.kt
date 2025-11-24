package com.siga.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object CategoriaTable : Table("siga_saas.CATEGORIAS") {
    
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100).uniqueIndex()
    val descripcion = text("descripcion").nullable()
    val activa = bool("activa").default(true)
    val fechaCreacion = timestamp("fecha_creacion").default(Instant.now())
}
