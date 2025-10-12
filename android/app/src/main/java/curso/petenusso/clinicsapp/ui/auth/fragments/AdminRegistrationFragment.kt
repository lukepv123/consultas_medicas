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
import curso.petenusso.clinicsapp.databinding.FragmentAdminRegistrationBinding
import kotlinx.coroutines.launch
import retrofit2.create

class AdminRegistrationFragment : Fragment() {

    private var _binding: FragmentAdminRegistrationBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy {
        val api = RetrofitFactory.retrofit().create<AdminApi>()
        AdminRepository(api)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnRegister.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtPassword.text.toString()

            binding.progress.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                when (val res = repo.createAdmin(email, senha)) {
                    is AppResult.Success -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Administrador cadastrado!", Toast.LENGTH_SHORT).show()
                        Navigator.toLogin(this@AdminRegistrationFragment)
                    }
                    is AppResult.Error -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), res.throwable.message ?: "Erro ao cadastrar", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}