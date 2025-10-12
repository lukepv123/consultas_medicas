package curso.petenusso.clinicsapp.model.session

data class MedicoSession(
    override val emailOrUser: String,
    override val authPassword: String,
    override val displayName: String? = null,
    override val userId: String? = null,
    override val roles: List<String> = listOf("MEDICO"),
    val medicoId: String? = null,
    val crm: String? = null,
    val nome: String? = displayName,
    val especialidade: String? = null
) : UserSession
