package com.consultasmedicas.consultas.medicas.controller.mapper

import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoResponseDTO
import com.consultasmedicas.consultas.medicas.model.Medico

object MedicoMapper {
    fun toDTO(e: Medico) = MedicoResponseDTO(
        id = e.id!!,
        crm = e.crm,
        nome = e.nome,
        email = e.email,
        especialidade = e.especialidade
    )
}