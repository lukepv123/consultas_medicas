package com.consultasmedicas.consultas.medicas.controller.dto.medicos

import com.consultasmedicas.consultas.medicas.model.Especialidade
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MedicoDTO(
    @field:NotBlank @field:Size(min = 3, max = 20)
    val crm: String,
    @field:NotBlank @field:Size(min = 2, max = 120)
    val nome: String,
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank @field:Size(min = 6, max = 80)
    val senha: String,
    val especialidade: Especialidade
)

