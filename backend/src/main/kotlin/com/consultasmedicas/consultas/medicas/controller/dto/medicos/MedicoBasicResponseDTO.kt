package com.consultasmedicas.consultas.medicas.controller.dto.medicos

data class MedicoBasicResponseDTO(
    val id: String,
    val crm: String,
    val nome: String,
    val especialidade: String
)