package com.consultasmedicas.consultas.medicas.controller.dto.medicos

import java.util.UUID


data class MedicoSessionDTO(
    val type: String = "MEDICO",
    val userId: UUID,
    val email: String,
    val roles: List<String>,
    val medico: MedicoEntityDTO
)