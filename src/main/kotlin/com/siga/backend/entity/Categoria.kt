package com.siga.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "CATEGORIAS", schema = "siga_saas")
data class Categoria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(nullable = false, unique = true, length = 100)
    val nombre: String,
    
    @Column(columnDefinition = "TEXT")
    val descripcion: String? = null,
    
    @Column(name = "usuario_comercial_id")
    val usuarioComercialId: Int? = null,  // ID del usuario comercial (dueño) al que pertenece
    
    @Column(nullable = false)
    val activa: Boolean = true,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: Instant = Instant.now()
)

