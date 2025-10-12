package curso.petenusso.clinicsapp.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.adm.AdminApi
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.adm.AdminRepository
import curso.petenusso.clinicsapp.databinding.FragmentLoginBinding
import curso.petenusso.clinicsapp.model.session.SessionManager
import kotlinx.coroutines.launch
import retrofit2.create

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy {
        val api = RetrofitFactory.retrofit().create<AdminApi>()
        AdminRepository(api)
    }

    // Aceita apenas minúsculas (ASCII) no e-mail
    private val EMAIL_LOWER_REGEX =
        Regex("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener {
            val emailRaw = binding.edtUser.text?.toString()?.trim().orEmpty()
            val senha = binding.edtPassword.text?.toString().orEmpty()

            // 1) Rejeita se houver qualquer maiúscula
            if (emailRaw != emailRaw.lowercase()) {
                binding.edtUser.error = "Use apenas letras minúsculas"
                Toast.makeText(requireContext(), "E-mail deve estar todo em minúsculas.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2) Validação de formato (minúsculas apenas)
            if (!EMAIL_LOWER_REGEX.matches(emailRaw)) {
                binding.edtUser.error = "E-mail inválido (somente minúsculas)"
                Toast.makeText(requireContext(), "E-mail inválido. Use apenas minúsculas.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progress.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                when (val res = repo.login(emailRaw, senha)) {
                    is AppResult.Success -> {
                        binding.progress.visibility = View.GONE
                        SessionManager.set(res.data)
                        Navigator.goToRole(requireContext(), res.data, finishCurrent = true)
                    }
                    is AppResult.Error -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            res.throwable.message ?: "Erro ao entrar",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
