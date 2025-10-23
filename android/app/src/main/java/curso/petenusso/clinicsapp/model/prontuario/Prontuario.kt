package curso.petenusso.clinicsapp.model.prontuario

data class Prontuario(
    val id: String,
    val idPaciente: String,
    val atendimento: String,
    val alergias: String,
    val deficiencia: String,
    val comorbidade: String,
    val exames: String,
    val medicacao: String,
    val dataCadastro: String
)
