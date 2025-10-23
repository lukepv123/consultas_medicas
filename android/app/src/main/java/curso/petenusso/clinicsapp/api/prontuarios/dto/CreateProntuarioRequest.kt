package curso.petenusso.clinicsapp.api.prontuarios.dto

data class CreateProntuarioRequest(
    val idPaciente: String,
    val atendimento: String,
    val alergias: String?,
    val deficiencia: String?,
    val comorbidade: String?,
    val exames: String?,
    val medicacao: String?
)
