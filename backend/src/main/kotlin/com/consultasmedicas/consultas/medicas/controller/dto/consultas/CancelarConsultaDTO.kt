package com.consultasmedicas.consultas.medicas.controller.dto.consultas

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class CancelarConsultaDTO (

    @field:NotBlank(message = "campo obrigatorio")
    val cpfPaciente: String,

    @field:NotNull(message = "campo obrigatorio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val dataHoraConsulta: OffsetDateTime,

    @field:NotBlank(message = "campo obrigatorio")
    val justificativa: String

)