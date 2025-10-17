package com.consultasmedicas.consultas.medicas.controller.dto.consultas

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class CancelarConsultaDTO (

    @field:NotBlank(message = "campo obrigatório")
    val idConsulta: String,

    @field:NotBlank(message = "campo obrigatorio")
    val cpfPaciente: String,

    @field:NotNull(message = "campo obrigatorio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val dataHoraConsulta: OffsetDateTime,

    // 🆕 opcional; limite de tamanho para evitar payloads gigantes
    @field:Size(max = 300, message = "justificativa deve ter no máximo 300 caracteres")
    val justificativa: String? = null

)