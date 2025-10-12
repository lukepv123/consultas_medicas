package curso.petenusso.clinicsapp.ui.adm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.core.Navigator

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        if (savedInstanceState == null) {
            Navigator.showAdminLobby(this)
        }
    }
}
