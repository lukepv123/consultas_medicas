package com.consultasmedicas.consultas.medicas.controller.dto.pacientes

import java.time.OffsetDateTime
import java.util.UUID

data class PacienteEntityDTO(
    val id: UUID,
    val cpf: String,
    val nome: String,
    val dataCadastro: OffsetDateTime,
    val dataUltimaAtualizacao: OffsetDateTime,
    val usuarioUltimaAtualizacao: String?
)