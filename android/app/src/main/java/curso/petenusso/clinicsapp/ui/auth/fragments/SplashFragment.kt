package curso.petenusso.clinicsapp.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.adm.AdminApi
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.adm.AdminRepository
import curso.petenusso.clinicsapp.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.create

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy {
        val api = RetrofitFactory.retrofit().create<AdminApi>()
        AdminRepository(api)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imgLogo.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 2000 })

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            when (val res = repo.status()) {
                is AppResult.Success ->
                    if (res.data.hasAdmin) Navigator.toLogin(this@SplashFragment)
                    else Navigator.toOnboarding(this@SplashFragment)
                is AppResult.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.txtError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
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

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}