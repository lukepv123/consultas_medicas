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
import curso.petenusso.clinicsapp.model.session.SessionManager
import curso.petenusso.clinicsapp.model.session.UserSession
import curso.petenusso.clinicsapp.ui.adm.AdminActivity
import curso.petenusso.clinicsapp.ui.adm.fragments.CadastrarMedicoAdmFragment
import curso.petenusso.clinicsapp.ui.adm.fragments.CancelarConsultaAdmFragment
import curso.petenusso.clinicsapp.ui.adm.fragments.LobbyAdmFragment
import curso.petenusso.clinicsapp.ui.auth.AuthActivity
import curso.petenusso.clinicsapp.ui.auth.fragments.AdminRegistrationFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.CriarContaPacienteFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.LoginFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.SplashFragment
import curso.petenusso.clinicsapp.ui.auth.fragments.WelcomeOnboardingFragment
import curso.petenusso.clinicsapp.ui.medico.MedicoActivity
import curso.petenusso.clinicsapp.ui.medico.fragments.LobbyMedicoFragment
import curso.petenusso.clinicsapp.ui.paciente.PacienteActivity
import curso.petenusso.clinicsapp.ui.paciente.fragments.AgendarConsultaPacienteFragment
import curso.petenusso.clinicsapp.ui.paciente.fragments.DadosPacienteFragment
import curso.petenusso.clinicsapp.ui.paciente.fragments.LobbyPacienteFragment
import curso.petenusso.clinicsapp.ui.paciente.fragments.MinhasConsultasPacienteFragment

object Navigator {

    // ======================================================
    // =============== 🔐 MÓDULO AUTH ========================
    // ======================================================

    @IdRes
    val AUTH_CONTAINER_ID: Int = R.id.authContainer

    fun showAuthSplash(activity: AppCompatActivity) {
        replaceFragment(activity, AUTH_CONTAINER_ID, SplashFragment(), addToBackStack = false)
    }

    fun toOnboarding(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, WelcomeOnboardingFragment())
    }

    fun toCriarPaciente(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, CriarContaPacienteFragment())
    }

    fun toLogin(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, LoginFragment())
    }


    fun toAdminRegistration(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, AUTH_CONTAINER_ID, AdminRegistrationFragment())
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



    // ======================================================
    // =============== 🧩 MÓDULO ADMIN =======================
    // ======================================================

    @IdRes
    val ADMIN_CONTAINER_ID: Int = R.id.adminContainer

    fun toLoginAdm(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, LoginFragment())
    }

    // ✅ boot do AdminActivity -> Lobby
    fun showAdminLobby(activity: AppCompatActivity) {
        replaceFragment(activity, ADMIN_CONTAINER_ID, LobbyAdmFragment(), addToBackStack = false)
    }

    // ✅ navegação entre fragments internos do admin
    fun toAdminCadastrarMedico(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, CadastrarMedicoAdmFragment())
    }

    fun toAdminCancelarConsulta(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, CancelarConsultaAdmFragment())
    }

    fun backToAdminLobby(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, ADMIN_CONTAINER_ID, LobbyAdmFragment(), addToBackStack = false)
    }


    // ======================================================
    // =============== 👩‍⚕️ MÓDULO PACIENTE ==================
    // ======================================================

    @IdRes
    val PACIENTE_CONTAINER_ID: Int = R.id.pacienteContainer

//    fun toLoginPaciente(fragment: Fragment) {
//        replaceFragment(fragment.requireActivity() as AppCompatActivity, PACIENTE_CONTAINER_ID, LoginFragment())
//    }

    fun toLoginPaciente(activity: AppCompatActivity) {
        replaceFragment(activity, AUTH_CONTAINER_ID, LoginFragment(), addToBackStack = false)
    }

    fun showPacienteLobby(activity: AppCompatActivity) {
        replaceFragment(activity, PACIENTE_CONTAINER_ID, LobbyPacienteFragment(), addToBackStack = false)
    }

    // navegação entre fragments internos do paciente

    fun showAgendarConsultaPaciente(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, PACIENTE_CONTAINER_ID, AgendarConsultaPacienteFragment())

    }

    fun showMinhasConsultasPaciente(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, PACIENTE_CONTAINER_ID, MinhasConsultasPacienteFragment())


    }

    fun showDadosPessoaisPaciente(host: Fragment) {
        replaceFragment(host.requireActivity() as AppCompatActivity, PACIENTE_CONTAINER_ID, DadosPacienteFragment())

    }


    // ======================================================
    // =============== 🩺 MÓDULO MÉDICO =======================
    // ======================================================

    @IdRes
    val MEDICO_CONTAINER_ID: Int = R.id.medicoContainer

    fun toLoginMedico(fragment: Fragment) {
        replaceFragment(fragment.requireActivity() as AppCompatActivity, MEDICO_CONTAINER_ID, LoginFragment())
    }

    fun showMedicoLobby(activity: AppCompatActivity) {
        replaceFragment(activity, MEDICO_CONTAINER_ID, LobbyMedicoFragment(), addToBackStack = false)
    }


    // ======================================================
    // =============== ⚙️ MÉTODO GENÉRICO =====================
    // ======================================================

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

    fun goBack(fragment: Fragment) {
        val activity = fragment.requireActivity()
        val fm = activity.supportFragmentManager

        // Se ainda há fragments na pilha, volta um nível
        if (fm.backStackEntryCount > 0) {
            fm.popBackStack()
        } else {
            // Se não há back stack, aciona comportamento padrão de "voltar"
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }




    fun logoutToLogin(context: Context) {
        val session = SessionManager.current
        SessionManager.clear()

        // Sempre abre AuthActivity limpa (login e onboarding)
        val intent = Intent(context, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        if (context is Activity) context.finish()

        // Log opcional
        println("Logout realizado com sucesso: ${session?.javaClass?.simpleName ?: "sem sessão"}")
    }
}
