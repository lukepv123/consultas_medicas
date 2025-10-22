package curso.petenusso.clinicsapp.api.medico

import curso.petenusso.clinicsapp.api.medico.dto.CreateMedicoRequest
import curso.petenusso.clinicsapp.api.medico.dto.CreateMedicoResponse
import curso.petenusso.clinicsapp.api.medico.dto.MedicoBasicResponse
import curso.petenusso.clinicsapp.api.medico.dto.MedicoDTO
import retrofit2.http.*
import curso.petenusso.clinicsapp.api.medico.dto.MedicoListResponse
import retrofit2.Response

interface MedicoApi {
    @POST("medicos")
    suspend fun criar(
        @Body req: CreateMedicoRequest
    ): CreateMedicoResponse

    @PUT("medicos/{id}")
    suspend fun atualizar(@Path("id") id: String, @Body body: MedicoDTO): MedicoDTO

    @GET("medicos/{id}")
    suspend fun buscar(@Path("id") id: String): MedicoDTO

    @DELETE("medicos/{id}")
    suspend fun remover(@Path("id") id: String)



    @GET("medicos")
    suspend fun listar(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("especialidade") especialidade: String? = null
    ): MedicoListResponse


    @GET("medicos/{id}/basico")
    suspend fun buscarBasico(
        @Path("id") id: String
    ): Response<MedicoBasicResponse>
}
