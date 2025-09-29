package com.consultasmedicas.consultas.medicas.controller.dto.medicos

import java.time.OffsetDateTime
import java.util.UUID

data class MedicoEntityDTO(
    val id: UUID,
    val crm: String,
    val nome: String,
    val especialidade: String,
    val dataCadastro: OffsetDateTime,
    val dataUltimaAtualizacao: OffsetDateTime,
    val usuarioUltimaAtualizacao: String?
)