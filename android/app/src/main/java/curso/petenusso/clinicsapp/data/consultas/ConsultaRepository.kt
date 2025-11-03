package curso.petenusso.clinicsapp.data.consultas

import curso.petenusso.clinicsapp.api.consulta.ConsultaApi
import curso.petenusso.clinicsapp.api.consulta.dto.*
import curso.petenusso.clinicsapp.core.AppResult

class ConsultaRepository {

    private val api = ConsultaApi.create()

    /** 🔹 Retorna consultas futuras do paciente (PageEnvelope) */
    suspend fun listarFuturasPorPaciente(idPaciente: String): AppResult<List<ConsultaResumoDTO>> {
        return try {
            val response = api.listarFuturasPorPacienteId(idPaciente)
            if (response.isSuccessful) {
                val lista = response.body()?.list() ?: emptyList()
                AppResult.Success(lista)
            } else {
                AppResult.Error(Throwable("Erro ${response.code()} ao listar futuras por paciente"))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Retorna consultas futuras de um médico (PageEnvelope) */
    suspend fun listarFuturasMedico(idMedico: String): AppResult<List<ConsultaDTO>> {
        return try {
            val response = api.listarFuturasMedico(idMedico)
            if (response.isSuccessful) {
                val lista = response.body()?.list() ?: emptyList()
                AppResult.Success(lista)
            } else {
                AppResult.Error(Throwable("Erro ${response.code()} ao listar futuras por médico"))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Retorna lista de futuras (ConsultaListResponse → usa .data) */
    suspend fun listarFuturas(idPaciente: String): AppResult<List<ConsultaDTO>> {
        return try {
            val response = api.listarFuturas(idPaciente)
            if (response.isSuccessful) {
                val lista = response.body()?.data ?: emptyList()
                AppResult.Success(lista)
            } else {
                AppResult.Error(Throwable("Erro ${response.code()} ao listar futuras"))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Retorna lista de passadas (ConsultaListResponse → usa .data) */
    suspend fun listarPassadas(idPaciente: String): AppResult<List<ConsultaDTO>> {
        return try {
            val response = api.listarPassadas(idPaciente)
            if (response.isSuccessful) {
                val lista = response.body()?.data ?: emptyList()
                AppResult.Success(lista)
            } else {
                AppResult.Error(Throwable("Erro ${response.code()} ao listar passadas"))
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Cadastra nova consulta (CreateConsultaRequest) */
    suspend fun cadastrar(body: CreateConsultaRequest): AppResult<Int> {
        return try {
            val response = api.cadastrar(body)
            AppResult.Success(response.code()) // retorna o código HTTP (ex: 201, 409 etc.)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /** 🔹 Cancela uma consulta existente */
    suspend fun cancelar(body: CancelarConsultaDTO): AppResult<Int> {
        return try {
            val response = api.cancelar(body)
            AppResult.Success(response.code())
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
