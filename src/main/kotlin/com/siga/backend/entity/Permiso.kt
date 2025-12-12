package com.siga.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "PERMISOS", schema = "siga_saas")
data class Permiso(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(nullable = false, unique = true, length = 50)
    val codigo: String,
    
    @Column(nullable = false, length = 100)
    val nombre: String,
    
    @Column(columnDefinition = "TEXT")
    val descripcion: String? = null,
    
    @Column(nullable = false, length = 50)
    val categoria: String,
    
    @Column(nullable = false)
    val activo: Boolean = true,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: java.time.Instant = java.time.Instant.now()
)
