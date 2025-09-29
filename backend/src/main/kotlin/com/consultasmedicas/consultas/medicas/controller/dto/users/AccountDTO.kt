package com.consultasmedicas.consultas.medicas.controller.dto.users


import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AccountDTO(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank @field:Size(min=6, max=80)
    val senha: String
)