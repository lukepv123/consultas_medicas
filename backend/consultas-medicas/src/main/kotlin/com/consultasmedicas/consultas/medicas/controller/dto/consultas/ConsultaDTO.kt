package com.consultasmedicas.consultas.medicas.controller.dto.consultas

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class ConsultaDTO(
    @field:NotNull(message = "campo obrigatorio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val dataHoraConsulta: OffsetDateTime,

    @field:NotNull(message = "campo obrigatorio")
    val idMedico: UUID,

    @field:NotNull(message = "campo obrigatorio")
    val idPaciente: UUID
)
