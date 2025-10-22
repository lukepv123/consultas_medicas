package curso.petenusso.clinicsapp.ui.medico

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.ActivityMedicoBinding

class MedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            Navigator.showMedicoLobby(this)
        }
    }
}
