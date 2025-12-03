package com.siga.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "USUARIOS", schema = "siga_saas")
data class UsuarioSaas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(nullable = false, unique = true, length = 255)
    val email: String,
    
    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String,
    
    @Column(nullable = false, length = 100)
    val nombre: String,
    
    @Column(length = 100)
    val apellido: String? = null,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val rol: Rol,
    
    @Column(nullable = false)
    val activo: Boolean = true,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: Instant = Instant.now(),
    
    @Column(name = "fecha_actualizacion", nullable = false)
    val fechaActualizacion: Instant = Instant.now()
)

enum class Rol {
    ADMINISTRADOR,
    OPERADOR,
    CAJERO
}

