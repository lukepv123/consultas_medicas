package curso.petenusso.clinicsapp.api.consulta

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.consulta.dto.CancelarConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaListResponse
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaResumoDTO
import curso.petenusso.clinicsapp.api.consulta.dto.CreateConsultaRequest
import curso.petenusso.clinicsapp.api.consulta.dto.PageEnvelope
import retrofit2.Response
import retrofit2.http.*

interface ConsultaApi {

    @GET("consultas/paciente/{idPaciente}/futuras")
    suspend fun listarFuturasPorPacienteId(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PageEnvelope<ConsultaResumoDTO>>

    @POST("consultas/cancelamento")
    suspend fun cancelar(@Body body: CancelarConsultaDTO): Response<Void>

    @POST("consultas")
    suspend fun cadastrar(@Body body: CreateConsultaRequest): Response<Unit>

    @GET("consultas/paciente/{idPaciente}/futuras")
    suspend fun listarFuturas(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): Response<ConsultaListResponse>

    @GET("consultas/paciente/{idPaciente}/passadas")
    suspend fun listarPassadas(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): Response<ConsultaListResponse>

    @GET("consultas/medico/{idMedico}/futuras")
    suspend fun listarFuturasMedico(
        @Path("idMedico") idMedico: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): Response<PageEnvelope<ConsultaDTO>>

    companion object {
        fun create(): ConsultaApi = RetrofitFactory.retrofit().create(ConsultaApi::class.java)
    }
}
