package com.consultasmedicas.consultas.medicas.controller.dto.pacientes


import com.consultasmedicas.consultas.medicas.controller.dto.users.AccountDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PacienteCreateDTO(
    @field:NotBlank @field:Size(min = 11, max = 14)
    val cpf: String,
    @field:NotBlank @field:Size(min = 2, max = 120)
    val nome: String,
    val account: AccountDTO // email+senha para criar User com ROLE_PACIENTE
)