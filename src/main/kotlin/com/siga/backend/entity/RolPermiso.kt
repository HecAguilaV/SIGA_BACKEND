package com.siga.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "ROLES_PERMISOS", schema = "siga_saas")
data class RolPermiso(
    @Id
    @EmbeddedId
    val id: RolPermisoId,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permiso_id", insertable = false, updatable = false)
    val permiso: Permiso? = null
)

@Embeddable
data class RolPermisoId(
    @Column(name = "rol", nullable = false, length = 20)
    val rol: String,
    
    @Column(name = "permiso_id", nullable = false)
    val permisoId: Int
) : java.io.Serializable
