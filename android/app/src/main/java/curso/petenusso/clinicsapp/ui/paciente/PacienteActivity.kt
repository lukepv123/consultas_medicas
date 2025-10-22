package curso.petenusso.clinicsapp.ui.paciente

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.ActivityPacienteBinding
import curso.petenusso.clinicsapp.model.session.SessionManager
import curso.petenusso.clinicsapp.ui.paciente.fragments.LobbyPacienteFragment

class PacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPacienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root) // ✅ usa o binding ao invés do setContentView tradicional

        // Exibe o fragment inicial apenas na primeira criação da Activity
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.pacienteContainer.id, LobbyPacienteFragment())
                .commit()
        }

    }

    /**
     * Faz logout explícito a partir de qualquer Fragment.
     */
    fun logoutToLogin() {
        SessionManager.clear()
        Navigator.logoutToLogin(this)
        finishAffinity()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && !isChangingConfigurations) {
            SessionManager.clear()
        }
    }
}
