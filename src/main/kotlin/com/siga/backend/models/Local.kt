package com.siga.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object LocalTable : Table("siga_saas.LOCALES") {
    
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val direccion = text("direccion").nullable()
    val ciudad = varchar("ciudad", 100).nullable()
    val activo = bool("activo").default(true)
    val fechaCreacion = timestamp("fecha_creacion").default(Instant.now())
}
