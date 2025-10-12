package curso.petenusso.clinicsapp.api.medico

import curso.petenusso.clinicsapp.api.medico.dto.CreateMedicoRequest
import curso.petenusso.clinicsapp.api.medico.dto.CreateMedicoResponse
import curso.petenusso.clinicsapp.api.medico.dto.MedicoDTO
import retrofit2.http.*

interface MedicoApi {
    @POST("medicos")
    suspend fun criar(
        @Body req: CreateMedicoRequest
    ): CreateMedicoResponse

    @PUT("medicos/{id}")
    suspend fun atualizar(@Path("id") id: String, @Body body: MedicoDTO): MedicoDTO

    @GET("medicos")
    suspend fun listar(): List<MedicoDTO>

    @GET("medicos/{id}")
    suspend fun buscar(@Path("id") id: String): MedicoDTO

    @DELETE("medicos/{id}")
    suspend fun remover(@Path("id") id: String)
}
