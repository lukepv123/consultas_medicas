package curso.petenusso.clinicsapp.api

import android.util.Base64
import curso.petenusso.clinicsapp.model.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val session = SessionManager.current ?: return chain.proceed(original)

        // email:senha do usuário logado (aqui, ADMIN)
        val creds = "${session.emailOrUser}:${session.authPassword}"
        val basic = "Basic " + Base64.encodeToString(creds.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        val req = original.newBuilder()
            .header("Authorization", basic)
            .build()

        return chain.proceed(req)
    }
}


