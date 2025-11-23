package curso.petenusso.clinicsapp.data.adm

import android.util.Base64
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.RetrofitFactoryPublic
import curso.petenusso.clinicsapp.api.adm.AdminApi
import curso.petenusso.clinicsapp.api.adm.dto.SetupAdminDTO
import curso.petenusso.clinicsapp.api.adm.dto.SetupStatusResponse
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.model.session.*

class AdminRepository {

    private val apiAuth = RetrofitFactory.retrofit().create(AdminApi::class.java)
    private val apiPublic = RetrofitFactoryPublic.retrofit().create(AdminApi::class.java)


    suspend fun status(): AppResult<SetupStatusResponse> = try {
        val response = apiPublic.getSetupStatus()
        if (response.isSuccessful && response.body() != null) {
            AppResult.Success(response.body()!!)
        } else {
            println("Erro HTTP: ${response.code()}, body: ${response.errorBody()?.string()}")
            AppResult.Error(Throwable("Erro ${response.code()} ao verificar status"))
        }
    } catch (e: Exception) {
        e.printStackTrace() // <- Isso é essencial pra entender o erro
        AppResult.Error(e)
    }

    /** 🔹 Cria o administrador inicial no sistema (público) */
    suspend fun createAdmin(email: String, senha: String): AppResult<String> = try {
        val response = apiPublic.createAdmin(SetupAdminDTO(email, senha))
        if (response.isSuccessful && response.body() != null) {
            AppResult.Success(response.body()!!.id)
        } else {
            AppResult.Error(Throwable("Erro ${response.code()} ao criar administrador"))
        }
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    /** 🔹 Realiza login e retorna sessão conforme o papel do usuário (autenticado) */
    suspend fun login(emailOrUser: String, password: String): AppResult<UserSession> = try {
        val authHeader = "Basic " + Base64.encodeToString(
            "$emailOrUser:$password".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )

        val payload = apiAuth.getSession(authHeader)
        val roles = (payload["roles"] as? List<*>)?.map { it.toString() } ?: emptyList()
        val userId = payload["userId"]?.toString()

        val session = when {
            "ADMIN" in roles -> AdmSession(
                emailOrUser = emailOrUser,
                authPassword = password,
                displayName = "Administrador",
                userId = userId,
                roles = roles
            )

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

            else -> error("Usuário sem role conhecida.")
        }

        AppResult.Success(session)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
