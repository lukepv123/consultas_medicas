package curso.petenusso.clinicsapp.api.prontuarios

import curso.petenusso.clinicsapp.api.prontuarios.dto.CreateProntuarioRequest
import curso.petenusso.clinicsapp.api.prontuarios.dto.ProntuarioDTO
import curso.petenusso.clinicsapp.api.prontuarios.dto.ProntuarioListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProntuarioApi {

    // ➕ Cadastrar prontuário
    @POST("prontuarios")
    suspend fun criar(
        @Body body: CreateProntuarioRequest
    ): Response<ProntuarioDTO>

    // 📋 Listar prontuários do paciente (paginado)
    @GET("prontuarios/paciente/{idPaciente}")
    suspend fun listarPorPaciente(
        @Path("idPaciente") idPaciente: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ProntuarioListResponse>




}

