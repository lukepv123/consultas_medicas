package curso.petenusso.clinicsapp.api.consulta

import curso.petenusso.clinicsapp.api.consulta.dto.CancelarConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaListResponse
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaResumoDTO
import curso.petenusso.clinicsapp.api.consulta.dto.CreateConsultaRequest
import curso.petenusso.clinicsapp.api.consulta.dto.PageEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ConsultaApi {

    // Futuras por ID do paciente
    @GET("consultas/paciente/{idPaciente}/futuras")
    suspend fun listarFuturasPorPacienteId(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PageEnvelope<ConsultaResumoDTO>>

    // Cancelar por ID do paciente
    @POST("consultas/cancelamento")
    suspend fun cancelar(
        @Body body: CancelarConsultaDTO
    ): Response<Void>

    @POST("consultas")
    suspend fun cadastrar(@Body body: CreateConsultaRequest): Response<Unit>


    @GET("consultas/paciente/{idPaciente}/futuras")
    suspend fun listarFuturas(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ConsultaListResponse>

    @GET("consultas/paciente/{idPaciente}/passadas")
    suspend fun listarPassadas(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ConsultaListResponse>




}
