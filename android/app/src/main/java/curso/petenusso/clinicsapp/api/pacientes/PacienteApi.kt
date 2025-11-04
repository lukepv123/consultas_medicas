package curso.petenusso.clinicsapp.api.pacientes

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.pacientes.dto.IdResponse
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteCreateRequest
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PacienteApi {

    @POST("pacientes")
    suspend fun cadastrarPaciente(@Body body: PacienteCreateRequest): Response<Void>

    @GET("pacientes/cpf/{cpf}/id")
    suspend fun buscarIdPorCpf(@Path("cpf") cpf: String): Response<IdResponse>

    @GET("pacientes/{id}")
    suspend fun buscar(@Path("id") id: String): Response<PacienteDTO>

    companion object {
        fun create(): PacienteApi = RetrofitFactory.retrofit().create(PacienteApi::class.java)
    }
}
