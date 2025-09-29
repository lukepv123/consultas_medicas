package com.consultasmedicas.consultas.medicas.controller.dto.pacientes

import java.util.UUID

data class PacienteSessionDTO(
    val type: String = "PACIENTE",
    val userId: UUID,
    val email: String,
    val roles: List<String>,
    val paciente: PacienteEntityDTO
)