package curso.petenusso.clinicsapp.api.prontuarios

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.prontuarios.dto.CreateProntuarioRequest
import curso.petenusso.clinicsapp.api.prontuarios.dto.ProntuarioDTO
import curso.petenusso.clinicsapp.api.prontuarios.dto.ProntuarioListResponse
import retrofit2.Response
import retrofit2.http.*

interface ProntuarioApi {

    @POST("prontuarios")
    suspend fun criar(@Body body: CreateProntuarioRequest): Response<ProntuarioDTO>

    @GET("prontuarios/paciente/{idPaciente}")
    suspend fun listarPorPaciente(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ProntuarioListResponse>

    companion object {
        fun create(): ProntuarioApi = RetrofitFactory.retrofit().create(ProntuarioApi::class.java)
    }
}
