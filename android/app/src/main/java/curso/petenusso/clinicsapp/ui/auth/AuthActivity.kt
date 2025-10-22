package curso.petenusso.clinicsapp.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.ActivityMainBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            Navigator.showAuthSplash(this)
        }
    }
}
