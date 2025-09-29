package com.consultasmedicas.consultas.medicas.controller.mapper

import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoResponseDTO
import com.consultasmedicas.consultas.medicas.model.Medico

object MedicoMapper {
    fun toDTO(e: Medico, email: String?): MedicoResponseDTO =
        MedicoResponseDTO(
            id = e.id!!,
            crm = e.crm,
            nome = e.nome,
            email = email ?: "", // mantém contrato não-nulo; ajuste se preferir nullable
            especialidade = e.especialidade
        )
}