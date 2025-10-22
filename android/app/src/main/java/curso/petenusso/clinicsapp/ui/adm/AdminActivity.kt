package curso.petenusso.clinicsapp.ui.adm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            Navigator.showAdminLobby(this)
        }
    }
}
