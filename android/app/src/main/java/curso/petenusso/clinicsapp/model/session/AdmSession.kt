package curso.petenusso.clinicsapp.model.session

data class AdmSession(
    override val emailOrUser: String,
    override val authPassword: String,
    override val displayName: String? = "Administrador",
    override val userId: String? = null,
    override val roles: List<String> = listOf("ADMIN")
) : UserSession
