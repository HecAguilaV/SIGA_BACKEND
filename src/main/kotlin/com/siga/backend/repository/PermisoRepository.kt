package com.siga.backend.repository

import com.siga.backend.entity.Permiso
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PermisoRepository : JpaRepository<Permiso, Int> {
    fun findByCodigo(codigo: String): Optional<Permiso>
    fun findByCategoria(categoria: String): List<Permiso>
    fun findByActivoTrue(): List<Permiso>
    fun findByCodigoIn(codigos: List<String>): List<Permiso>
}
