package com.consultasmedicas.consultas.medicas.repository

import com.consultasmedicas.consultas.medicas.model.Paciente
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PacienteRepository : JpaRepository<Paciente, UUID> {
    fun existsByCpf(cpf: String): Boolean

    fun findByCpf(cpf: String): Optional<Paciente>


    // Compara o CPF desmascarado no BD com os dígitos puros da consulta
    @Query(
        """
        select p.id 
          from Paciente p
         where replace(replace(p.cpf, '.', ''), '-', '') = :digits
        """
    )
    fun findIdByCpfDigits(@Param("digits") digits: String): Optional<UUID>
}