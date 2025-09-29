package com.consultasmedicas.consultas.medicas.controller.dto.medicos

import com.consultasmedicas.consultas.medicas.controller.dto.users.AccountDTO
import com.consultasmedicas.consultas.medicas.model.Especialidade

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MedicoCreateDTO(
    @field:NotBlank @field:Size(min = 3, max = 20)
    val crm: String,
    @field:NotBlank @field:Size(min = 2, max = 120)
    val nome: String,
    val especialidade: Especialidade,
    val account: AccountDTO // email+senha para criar User com ROLE_MEDICO
)