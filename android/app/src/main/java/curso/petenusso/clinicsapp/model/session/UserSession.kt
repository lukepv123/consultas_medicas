package curso.petenusso.clinicsapp.model.session

sealed interface UserSession {
    val emailOrUser: String
    val authPassword: String
    val displayName: String?
    val userId: String?
    val roles: List<String>
}
