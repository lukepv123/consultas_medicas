package curso.petenusso.clinicsapp.api.consulta.dto

data class ConsultaDTO(

    val id: String,
    val dataHoraConsulta: String,
    val idMedico: String,
    val idPaciente: String,
    val status: String
)
