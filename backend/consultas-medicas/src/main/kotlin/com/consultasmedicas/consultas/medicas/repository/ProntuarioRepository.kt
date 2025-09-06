package com.consultasmedicas.consultas.medicas.repository

import com.consultasmedicas.consultas.medicas.model.Prontuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProntuarioRepository : JpaRepository<Prontuario, UUID> {
    fun findByIdPaciente(idPaciente: UUID, pageable: Pageable): Page<Prontuario>
}