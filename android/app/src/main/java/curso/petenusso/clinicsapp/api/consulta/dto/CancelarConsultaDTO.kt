package curso.petenusso.clinicsapp.api.consulta.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CancelarConsultaDTO(
    val cpfPaciente: String, // UUID do paciente
    val dataHoraConsulta: String,   // ISO-8601 exatamente como veio no GET
    val justificativa: String
)