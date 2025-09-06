package com.consultasmedicas.consultas.medicas.controller.dto.prontuarios

import java.time.OffsetDateTime
import java.util.UUID

data class ProntuarioResponseDTO(
    val id: UUID,
    val idPaciente: UUID,
    val atendimento: String,
    val alergias: String? = null,
    val deficiencia: String? = null,
    val comorbidade: String? = null,
    val exames: String? = null,
    val medicacao: String? = null,
    val dataCadastro: OffsetDateTime,
    val dataUltimaAtualizacao: OffsetDateTime,
    val usuarioUltimaAtualizacao: String?
)