package curso.petenusso.clinicsapp.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.core.Navigator

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usaremos o seu activity_main.xml como container de auth
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            Navigator.showAuthSplash(this)
        }
    }
}