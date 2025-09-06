package com.consultasmedicas.consultas.medicas.controller.dto.consultas

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.OffsetDateTime
import java.util.UUID

data class ConsultaResponseDTO(
    val id: UUID,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val dataHoraConsulta: OffsetDateTime,
    val idMedico: UUID,
    val idPaciente: UUID,
    val status: String
)