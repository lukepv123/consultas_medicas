package curso.petenusso.clinicsapp.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.model.session.AdmSession
import curso.petenusso.clinicsapp.model.session.MedicoSession
import curso.petenusso.clinicsapp.model.session.PacienteSession
import curso.petenusso.clinicsapp.model.session.UserSession
import curso.petenusso.clinicsapp.ui.adm.AdminActivity
import curso.petenusso.clinicsapp.ui.adm.fragments.CadastrarMedicoAdmFragment
import curso.petenusso.clinicsapp.ui.adm.fragments.CancelarConsultaAdmFragment
import curso.petenusso.clinicsapp.ui.adm.fragments.LobbyAdmFragment
import curso.petenusso.clinicsapp.ui.medico.MedicoActivity
import curso.petenusso.clinicsapp.ui.paciente.PacienteActivity
import curso.petenusso.clinicsapp.ui.auth.fragments.AdminRegistrationFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.LoginFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.SplashFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.WelcomeOnboardingFragment

object Navigator {
    @IdRes
    val AUTH_CONTAINER_ID: Int = R.id.authContainer

    fun showAuthSplash(activity: AppCompatActivity) {
        replaceFragment(activity, AUTH_CONTAINER_ID, SplashFragment(), addToBackStack = false)
    }

    fun toOnboarding(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, WelcomeOnboardingFragment())
    }

    fun toAdminRegistration(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, AdminRegistrationFragment())
    }

    fun toLogin(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, LoginFragment())
    }

    fun goToRole(context: Context, session: UserSession, finishCurrent: Boolean = true) {
        val intent = when (session) {
            is AdmSession      -> Intent(context, AdminActivity::class.java)
            is MedicoSession   -> Intent(context, MedicoActivity::class.java)
            is PacienteSession -> Intent(context, PacienteActivity::class.java)
        }
        context.startActivity(intent)
        if (finishCurrent && context is Activity) context.finish()
    }



//    Modulo adm


    // ✅ novo: container do módulo admin
    @IdRes
    val ADMIN_CONTAINER_ID: Int = R.id.adminContainer



    fun toLoginAdm(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, LoginFragment())
    }

    // ✅ boot do AdminActivity -> Lobby
    fun showAdminLobby(activity: AppCompatActivity) {
        replaceFragment(activity, ADMIN_CONTAINER_ID, LobbyAdmFragment(), addToBackStack = false)
    }

    // ✅ navegação entre os fragments internos do admin
    fun toAdminCadastrarMedico(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, CadastrarMedicoAdmFragment())
    }
    fun toAdminCancelarConsulta(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, CancelarConsultaAdmFragment())
    }
    fun backToAdminLobby(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, LobbyAdmFragment(), addToBackStack = false)
    }














    fun replaceFragment(
        activity: AppCompatActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        addToBackStack: Boolean = true
    ) {
        activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .replace(containerId, fragment, fragment::class.java.simpleName)
            .apply { if (addToBackStack) addToBackStack(fragment::class.java.simpleName) }
            .commit()
    }
}
