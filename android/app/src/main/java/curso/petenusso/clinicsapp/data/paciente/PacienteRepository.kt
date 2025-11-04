package curso.petenusso.clinicsapp.data.paciente

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.RetrofitFactoryPublic
import curso.petenusso.clinicsapp.api.pacientes.PacienteApi
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteCreateRequest
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteDTO
import curso.petenusso.clinicsapp.core.AppResult

class PacienteRepository {

    // 🔒 API autenticada (usa AuthInterceptor)
    private val apiAuth = RetrofitFactory.retrofit().create(PacienteApi::class.java)

    // 🔓 API pública (sem AuthInterceptor)
    private val apiPublic = RetrofitFactoryPublic.retrofit().create(PacienteApi::class.java)

    /** 🔹 Cadastra novo paciente (público) */
    suspend fun cadastrar(body: PacienteCreateRequest): AppResult<Int> {
        return try {
            val response = apiPublic.cadastrarPaciente(body)
            AppResult.Success(response.code())
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Busca paciente completo pelo ID (autenticado) */
    suspend fun buscarPaciente(id: String): AppResult<PacienteDTO> {
        return try {
            val response = apiAuth.buscar(id)
            if (response.isSuccessful && response.body() != null) {
                AppResult.Success(response.body()!!)
            } else {
                val msg = when (response.code()) {
                    401 -> "Não autorizado"
                    403 -> "Acesso negado"
                    404 -> "Paciente não encontrado"
                    else -> "Erro desconhecido (${response.code()})"
                }
                AppResult.Error(Throwable(msg))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Busca apenas o ID do paciente pelo CPF (autenticado) */
    suspend fun buscarIdPorCpf(cpf: String): AppResult<String> {
        return try {
            val response = apiAuth.buscarIdPorCpf(cpf)
            if (response.isSuccessful) {
                val id = response.body()?.id
                if (!id.isNullOrBlank()) {
                    AppResult.Success(id)
                } else {
                    AppResult.Error(Throwable("Paciente não encontrado"))
                }
            } else {
                val msg = when (response.code()) {
                    404 -> "Paciente não encontrado"
                    400 -> "CPF inválido"
                    else -> "Erro ${response.code()} ao buscar paciente"
                }
                AppResult.Error(Throwable(msg))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
