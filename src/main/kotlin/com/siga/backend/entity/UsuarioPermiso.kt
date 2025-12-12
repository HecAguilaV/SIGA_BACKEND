package com.siga.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "USUARIOS_PERMISOS", schema = "siga_saas")
data class UsuarioPermiso(
    @Id
    @EmbeddedId
    val id: UsuarioPermisoId,
    
    @Column(name = "fecha_asignacion", nullable = false)
    val fechaAsignacion: Instant = Instant.now(),
    
    @Column(name = "asignado_por")
    val asignadoPor: Int? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    val usuario: UsuarioSaas? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permiso_id", insertable = false, updatable = false)
    val permiso: Permiso? = null
)

@Embeddable
data class UsuarioPermisoId(
    @Column(name = "usuario_id", nullable = false)
    val usuarioId: Int,
    
    @Column(name = "permiso_id", nullable = false)
    val permisoId: Int
) : java.io.Serializable
