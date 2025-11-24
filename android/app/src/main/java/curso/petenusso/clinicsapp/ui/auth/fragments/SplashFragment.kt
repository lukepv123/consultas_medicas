package curso.petenusso.clinicsapp.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.adm.AdminRepository
import curso.petenusso.clinicsapp.data.firebase.AdmFirebaseRepository
import curso.petenusso.clinicsapp.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    // 🔹 Centralizado — o repositório gerencia o Retrofit e o tipo de autenticação
    private val repo by lazy { AdminRepository() }
    private val repoFirebase by lazy {AdmFirebaseRepository()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 🌀 Animação do logo
        binding.imgLogo.setImageResource(R.drawable.logo_clinics)
        binding.imgLogo.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 2000 })

        // ⚙️ Verificação do setup
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            when (val res = repoFirebase.status()) {
                is AppResult.Success -> {
                    if (res.data.hasAdmin)
                        Navigator.toLogin(this@SplashFragment)
                    else
                        Navigator.toOnboarding(this@SplashFragment)
                }

                is AppResult.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.txtError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE

                    binding.txtError.text =
                        res.throwable.localizedMessage ?: "Erro ao conectar ao servidor."

                    binding.btnRetry.setOnClickListener {
                        Navigator.replaceFragment(
                            requireActivity() as androidx.appcompat.app.AppCompatActivity,
                            Navigator.AUTH_CONTAINER_ID,
                            SplashFragment(),
                            addToBackStack = false
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
