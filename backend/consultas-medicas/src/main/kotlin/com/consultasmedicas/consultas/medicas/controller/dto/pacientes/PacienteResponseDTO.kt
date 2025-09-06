package com.consultasmedicas.consultas.medicas.controller.dto.pacientes

import java.util.UUID

data class PacienteResponseDTO(
    val id: UUID,
    val cpf: String,
    val nome: String,
    val email: String
)