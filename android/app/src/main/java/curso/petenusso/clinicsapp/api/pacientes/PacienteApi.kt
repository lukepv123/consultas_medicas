package curso.petenusso.clinicsapp.api.pacientes


import curso.petenusso.clinicsapp.api.pacientes.dto.IdResponse
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PacienteApi {

    // ✅ Novo endpoint para criar paciente
    @POST("pacientes")
    suspend fun cadastrarPaciente(
        @Body body: PacienteCreateRequest
    ): Response<Void>

    // CPF -> ID
    @GET("pacientes/cpf/{cpf}/id")
    suspend fun buscarIdPorCpf(
        @Path("cpf") cpf: String
    ): Response<IdResponse>
}