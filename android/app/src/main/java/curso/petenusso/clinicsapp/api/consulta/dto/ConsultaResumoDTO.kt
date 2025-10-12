package curso.petenusso.clinicsapp.api.consulta.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConsultaResumoDTO(
    val id: String? = null,
    @Json(name = "dataHoraConsulta") val dataHoraConsulta: String? = null, // ex.: "2025-10-23T18:00:00Z"
    val idMedico: String? = null,
    val idPaciente: String? = null,
    val status: String? = null
)