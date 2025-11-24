package com.siga.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UsuarioSaasTable : Table("siga_saas.usuarios") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val nombre = varchar("nombre", 100)
    val apellido = varchar("apellido", 100).nullable()
    val rol = varchar("rol", 20)
    val activo = bool("activo").default(true)
    val fechaCreacion = timestamp("fecha_creacion").default(Instant.now())
    val fechaActualizacion = timestamp("fecha_actualizacion").default(Instant.now())
}

enum class Rol {
    ADMINISTRADOR,
    OPERADOR,
    CAJERO
}