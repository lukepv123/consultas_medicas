package curso.petenusso.clinicsapp.api.adm

import curso.petenusso.clinicsapp.api.adm.dto.IdResponse
import curso.petenusso.clinicsapp.api.adm.dto.SetupAdminDTO
import curso.petenusso.clinicsapp.api.adm.dto.SetupStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AdminApi {

//    Auth
    @GET("setup/status")
    suspend fun getSetupStatus(): SetupStatusResponse  // { hasAdmin: boolean }

    @POST("setup/admin")
    suspend fun createAdmin(@Body body: SetupAdminDTO): IdResponse // { id: "<uuid>" }

    @GET("auth/session")
    suspend fun getSession(@Header("Authorization") basicAuth: String): Map<String, @JvmSuppressWildcards Any?>


}