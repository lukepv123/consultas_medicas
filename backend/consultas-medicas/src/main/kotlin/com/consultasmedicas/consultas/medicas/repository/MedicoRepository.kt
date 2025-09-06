package com.consultasmedicas.consultas.medicas.repository

import com.consultasmedicas.consultas.medicas.model.Especialidade
import com.consultasmedicas.consultas.medicas.model.Medico
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MedicoRepository : JpaRepository<Medico, UUID> {
    fun existsByCrm(crm: String): Boolean
    fun findByEspecialidade(especialidade: Especialidade, pageable: Pageable): Page<Medico>
}