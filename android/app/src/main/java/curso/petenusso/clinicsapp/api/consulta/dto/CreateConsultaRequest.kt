package curso.petenusso.clinicsapp.api.consulta.dto

data class CreateConsultaRequest(
    val dataHoraConsulta: String,
    val idMedico: String,
    val idPaciente: String
)
