package curso.petenusso.clinicsapp.data.prontuario

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.prontuarios.ProntuarioApi
import curso.petenusso.clinicsapp.api.prontuarios.dto.CreateProntuarioRequest
import curso.petenusso.clinicsapp.api.prontuarios.dto.ProntuarioDTO
import curso.petenusso.clinicsapp.core.AppResult

class ProntuarioRepository {

    // ✅ Criação correta da API com RetrofitFactory.retrofit()
    private val api = RetrofitFactory.retrofit().create(ProntuarioApi::class.java)

    suspend fun listarPorPaciente(idPaciente: String): AppResult<List<ProntuarioDTO>> {
        return try {
            val response = api.listarPorPaciente(idPaciente)
            if (response.isSuccessful) {
                AppResult.Success(response.body()?.data ?: emptyList())
            } else {
                AppResult.Error(Throwable("Erro: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    suspend fun criarProntuario(body: CreateProntuarioRequest): AppResult<ProntuarioDTO> {
        return try {
            val response = api.criar(body)
            if (response.isSuccessful && response.body() != null) {
                AppResult.Success(response.body()!!)
            } else {
                AppResult.Error(Throwable("Erro ao cadastrar prontuário (${response.code()})"))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
