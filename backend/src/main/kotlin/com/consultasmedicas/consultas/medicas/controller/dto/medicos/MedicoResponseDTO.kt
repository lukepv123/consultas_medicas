package com.consultasmedicas.consultas.medicas.controller.dto.medicos

import com.consultasmedicas.consultas.medicas.model.Especialidade
import java.util.UUID

data class MedicoResponseDTO(
    val id: UUID,
    val crm: String,
    val nome: String,
    val email: String?, // vem do User vinculado
    val especialidade: Especialidade
)