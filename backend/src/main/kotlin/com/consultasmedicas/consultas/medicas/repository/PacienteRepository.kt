package com.consultasmedicas.consultas.medicas.repository

import com.consultasmedicas.consultas.medicas.model.Paciente
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PacienteRepository : JpaRepository<Paciente, UUID> {
    fun existsByCpf(cpf: String): Boolean

    fun findByCpf(cpf: String): Optional<Paciente>
}