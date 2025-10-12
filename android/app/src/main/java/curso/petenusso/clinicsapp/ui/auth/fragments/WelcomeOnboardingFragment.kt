package curso.petenusso.clinicsapp.ui.auth.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.FragmentWelcomeOnboardingBinding

class WelcomeOnboardingFragment : Fragment() {
    private var _binding: FragmentWelcomeOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnStart.setOnClickListener { Navigator.toAdminRegistration(this) }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}