package com.consultasmedicas.consultas.medicas.controller.dto.pacientes

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PacienteDTO(
    @field:NotBlank(message = "campo obrigatorio")
    @field:Size(min = 11, max = 14, message = "tamanho invalido")
    val cpf: String,

    @field:NotBlank(message = "campo obrigatorio")
    @field:Size(min = 2, max = 120, message = "tamanho invalido")
    val nome: String,

    @field:NotBlank(message = "campo obrigatorio")
    @field:Email(message = "email invalido")
    val email: String,

    @field:NotBlank(message = "campo obrigatorio")
    @field:Size(min = 6, max = 80, message = "tamanho invalido")
    val senha: String
)
