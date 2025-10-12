package com.consultasmedicas.consultas.medicas.controller.dto.consultas

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class CancelarConsultaPorCpfDTO(
    @field:NotBlank @field:Size(min = 11, max = 14)
    val cpfPaciente: String,
    val dataHoraConsulta: OffsetDateTime,
    @field:NotBlank
    val justificativa: String
)