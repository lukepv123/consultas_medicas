package curso.petenusso.clinicsapp.data.adm

import android.util.Base64
import curso.petenusso.clinicsapp.api.adm.AdminApi
import curso.petenusso.clinicsapp.api.adm.dto.SetupAdminDTO
import curso.petenusso.clinicsapp.api.adm.dto.SetupStatusResponse
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.runCatchingResult
import curso.petenusso.clinicsapp.model.session.*

class AdminRepository(private val api: AdminApi) {

    suspend fun status(): AppResult<SetupStatusResponse> =
        runCatchingResult { api.getSetupStatus() }

    suspend fun createAdmin(email: String, senha: String): AppResult<String> =
        runCatchingResult { api.createAdmin(SetupAdminDTO(email, senha)).id }

    suspend fun login(emailOrUser: String, password: String): AppResult<UserSession> =
        runCatchingResult {
            val auth = "Basic " + Base64.encodeToString(
                "$emailOrUser:$password".toByteArray(Charsets.UTF_8), Base64.NO_WRAP
            )
            val payload = api.getSession(auth) // Map<String, Any?>

            val roles = (payload["roles"] as? List<*>)?.map { it.toString() } ?: emptyList()
            val userId = payload["userId"]?.toString()

            when {
                "ADMIN" in roles -> {
                    AdmSession(
                        emailOrUser = emailOrUser,
                        authPassword = password,
                        displayName = "Administrador",
                        userId = userId,
                        roles = roles
                    )
                }
                "MEDICO" in roles -> {
                    val medico = payload["medico"] as? Map<*, *>
                    MedicoSession(
                        emailOrUser = emailOrUser,
                        authPassword = password,
                        displayName = medico?.get("nome")?.toString() ?: "Médico",
                        userId = userId,
                        roles = roles,
                        medicoId = medico?.get("id")?.toString(),
                        crm = medico?.get("crm")?.toString(),
                        nome = medico?.get("nome")?.toString(),
                        especialidade = medico?.get("especialidade")?.toString()
                    )
                }
                "PACIENTE" in roles -> {
                    val paciente = payload["paciente"] as? Map<*, *>
                    PacienteSession(
                        emailOrUser = emailOrUser,
                        authPassword = password,
                        displayName = paciente?.get("nome")?.toString() ?: "Paciente",
                        userId = userId,
                        roles = roles,
                        pacienteId = paciente?.get("id")?.toString(),
                        nome = paciente?.get("nome")?.toString(),
                        cpf = paciente?.get("cpf")?.toString()
                    )
                }
                else -> error("Usuário sem role conhecida")
            }
        }
}