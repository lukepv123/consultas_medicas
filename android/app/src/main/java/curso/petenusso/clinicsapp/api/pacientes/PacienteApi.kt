package curso.petenusso.clinicsapp.api.pacientes


import curso.petenusso.clinicsapp.api.pacientes.dto.IdResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PacienteApi {

    // CPF -> ID
    @GET("pacientes/cpf/{cpf}/id")
    suspend fun buscarIdPorCpf(
        @Path("cpf") cpf: String
    ): Response<IdResponse>
}