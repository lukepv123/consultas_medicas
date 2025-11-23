package curso.petenusso.clinicsapp.api.adm

import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.adm.dto.IdResponse
import curso.petenusso.clinicsapp.api.adm.dto.SetupAdminDTO
import curso.petenusso.clinicsapp.api.adm.dto.SetupStatusResponse
import curso.petenusso.clinicsapp.api.prontuarios.ProntuarioApi
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AdminApi {

    /** 🔹 Retorna o status de configuração inicial do sistema */
    @GET("setup/status")
    suspend fun getSetupStatus(): Response<SetupStatusResponse>

    /** 🔹 Cria o administrador inicial */
    @POST("setup/admin")
    suspend fun createAdmin(@Body body: SetupAdminDTO): Response<IdResponse>

    /** 🔹 Retorna a sessão do usuário autenticado */
    @GET("auth/session")
    suspend fun getSession(@Header("Authorization") basicAuth: String): Map<String, @JvmSuppressWildcards Any?>


    companion object {
        fun create(): AdminApi = RetrofitFactory.retrofit().create(AdminApi::class.java)
    }



}
