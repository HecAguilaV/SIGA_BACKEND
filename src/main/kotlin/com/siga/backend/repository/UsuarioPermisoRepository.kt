package com.siga.backend.repository

import com.siga.backend.entity.UsuarioPermiso
import com.siga.backend.entity.UsuarioPermisoId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UsuarioPermisoRepository : JpaRepository<UsuarioPermiso, UsuarioPermisoId> {
    fun findByUsuarioId(usuarioId: Int): List<UsuarioPermiso>
    
    fun existsById_UsuarioIdAndId_PermisoId(usuarioId: Int, permisoId: Int): Boolean
}
