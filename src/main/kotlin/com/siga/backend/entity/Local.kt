package com.siga.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "LOCALES", schema = "siga_saas")
data class Local(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(nullable = false, length = 100)
    val nombre: String,
    
    @Column(columnDefinition = "TEXT")
    val direccion: String? = null,
    
    @Column(length = 100)
    val ciudad: String? = null,
    
    @Column(nullable = false)
    val activo: Boolean = true,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: Instant = Instant.now()
)

