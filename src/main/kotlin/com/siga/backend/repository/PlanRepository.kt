package com.siga.backend.repository

import com.siga.backend.entity.Plan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlanRepository : JpaRepository<Plan, Int> {
    fun findByActivoTrueOrderByOrdenAsc(): List<Plan>
}

