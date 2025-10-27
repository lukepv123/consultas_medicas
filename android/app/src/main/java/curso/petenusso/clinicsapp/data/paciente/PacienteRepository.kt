package curso.petenusso.clinicsapp.data.paciente

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.pacientes.PacienteApi
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteDTO
import curso.petenusso.clinicsapp.core.AppResult

class PacienteRepository {
    private val api = RetrofitFactory.retrofit().create(PacienteApi::class.java)

    suspend fun buscarPaciente(id: String): AppResult<PacienteDTO> {
        return try {
            val response = api.buscar(id)
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
}


