package com.consultasmedicas.consultas.medicas.repository

import com.consultasmedicas.consultas.medicas.model.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRoleRepository : JpaRepository<UserRole, UUID> {
    fun findByUserId(userId: UUID): List<UserRole>
    fun existsByRole(role: String): Boolean

}