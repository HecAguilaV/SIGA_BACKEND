package com.siga.backend.repository

import com.siga.backend.entity.Categoria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoriaRepository : JpaRepository<Categoria, Int> {
    fun findByActivaTrue(): List<Categoria>
    fun findByActivaTrueAndUsuarioComercialId(usuarioComercialId: Int): List<Categoria>
    fun findByUsuarioComercialId(usuarioComercialId: Int): List<Categoria>
    fun existsByNombre(nombre: String): Boolean
    fun existsByNombreAndUsuarioComercialId(nombre: String, usuarioComercialId: Int): Boolean
}
