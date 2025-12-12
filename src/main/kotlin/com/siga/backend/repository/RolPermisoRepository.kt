package com.siga.backend.repository

import com.siga.backend.entity.RolPermiso
import com.siga.backend.entity.RolPermisoId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RolPermisoRepository : JpaRepository<RolPermiso, RolPermisoId> {
    fun findByRol(rol: String): List<RolPermiso>
    
    @Query("SELECT rp FROM RolPermiso rp WHERE rp.id.rol = :rol AND rp.id.permisoId = :permisoId")
    fun existsByRolAndPermisoId(@Param("rol") rol: String, @Param("permisoId") permisoId: Int): Boolean
    
    fun existsById_RolAndId_PermisoId(rol: String, permisoId: Int): Boolean
}
