package com.siga.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "STOCK", 
    schema = "siga_saas",
    uniqueConstraints = [UniqueConstraint(columnNames = ["producto_id", "local_id"])]
)
data class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(name = "producto_id", nullable = false)
    val productoId: Int,
    
    @Column(name = "local_id", nullable = false)
    val localId: Int,
    
    @Column(nullable = false)
    val cantidad: Int = 0,
    
    @Column(name = "cantidad_minima", nullable = false)
    val cantidadMinima: Int = 0,
    
    @Column(name = "fecha_actualizacion", nullable = false)
    val fechaActualizacion: Instant = Instant.now()
)

