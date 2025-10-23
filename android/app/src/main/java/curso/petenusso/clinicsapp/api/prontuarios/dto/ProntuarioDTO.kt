package curso.petenusso.clinicsapp.api.prontuarios.dto

data class ProntuarioDTO(
    val id: String,
    val idPaciente: String,
    val atendimento: String,
    val alergias: String?,
    val deficiencia: String?,
    val comorbidade: String?,
    val exames: String?,
    val medicacao: String?,
    val dataCadastro: String,
    val dataUltimaAtualizacao: String?,
    val usuarioUltimaAtualizacao: String?

)
