package curso.petenusso.clinicsapp.model.session

data class PacienteSession(
    override val emailOrUser: String,
    override val authPassword: String,
    override val displayName: String? = null,
    override val userId: String? = null,
    override val roles: List<String> = listOf("PACIENTE"),
    val pacienteId: String? = null,
    val nome: String? = displayName,
    val cpf: String? = null
) : UserSession
