package curso.petenusso.clinicsapp.model.session

object SessionManager {
    @Volatile
    var current: UserSession? = null
        private set

    fun set(session: UserSession) { current = session }
    fun clear() { current = null }

    fun asAdm()      = current as? AdmSession
    fun asMedico()   = current as? MedicoSession
    fun asPaciente() = current as? PacienteSession
}
