package curso.petenusso.clinicsapp.ui.auth.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.InputFilter
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.adm.AdminApi
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.adm.AdminRepository
import curso.petenusso.clinicsapp.databinding.FragmentAdminRegistrationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.create

class AdminRegistrationFragment : Fragment() {

    private var _binding: FragmentAdminRegistrationBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy {
        val api = RetrofitFactory.retrofit().create<AdminApi>()
        AdminRepository(api)
    }

    companion object {
        private const val EMAIL_MAX = 120
        private const val PASS_MIN = 8
        private const val PASS_MAX = 80
        private val PASS_REGEX =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupValidation()
        setupActions()
    }

    private fun setupActions() = with(binding) {
        btnRegister.setOnClickListener { cadastrarAdmin() }
    }

    private fun setupValidation() = with(binding) {
        // Limites
        edtEmail.filters = arrayOf(InputFilter.LengthFilter(EMAIL_MAX))
        edtPassword.filters = arrayOf(InputFilter.LengthFilter(PASS_MAX))
        edtConfirmPassword.filters = arrayOf(InputFilter.LengthFilter(PASS_MAX))

        // Validação dinâmica
        edtEmail.doAfterTextChanged {
            val email = it?.toString()?.trim().orEmpty()
            edtEmail.error = if (email.isNotEmpty() &&
                (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length > EMAIL_MAX)
            ) "E-mail inválido" else null
        }

        edtPassword.doAfterTextChanged { validatePasswords() }
        edtConfirmPassword.doAfterTextChanged { validatePasswords() }
    }

    // =====================================================
    // 🧠 Validação visual das senhas + ícone animado
    // =====================================================
    private fun validatePasswords() = with(binding) {
        val senha = edtPassword.text?.toString().orEmpty()
        val confirmar = edtConfirmPassword.text?.toString().orEmpty()

        edtPassword.error = when {
            senha.isEmpty() -> null
            senha.length !in PASS_MIN..PASS_MAX ->
                "Senha deve ter $PASS_MIN–$PASS_MAX caracteres"
            !senha.matches(PASS_REGEX) ->
                "Use maiús., minús., número e símbolo"
            else -> null
        }

        // Mostra o ícone apenas quando o usuário digita algo
        ivPasswordMatchStatus.visibility = if (confirmar.isNotEmpty()) View.VISIBLE else View.GONE

        // Atualiza o ícone conforme a correspondência
        if (confirmar.isNotEmpty()) {
            val iconRes = if (confirmar == senha) R.drawable.ic_check_green else R.drawable.ic_check_red
            ivPasswordMatchStatus.setImageResource(iconRes)

            // Animação suave (fade)
            ObjectAnimator.ofFloat(ivPasswordMatchStatus, "alpha", 0f, 1f).apply {
                duration = 250
                start()
            }
        }
    }

    // =====================================================
    // 🔒 Validação completa antes do envio
    // =====================================================
    private fun validateAll(): Boolean = with(binding) {
        val email = edtEmail.text.toString().trim().lowercase()
        val senha = edtPassword.text.toString()
        val confirmar = edtConfirmPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length > EMAIL_MAX) {
            toast("E-mail inválido.")
            return false
        }
        if (senha.length !in PASS_MIN..PASS_MAX || !senha.matches(PASS_REGEX)) {
            toast("Senha fraca. Use maiús., minús., número e símbolo.")
            return false
        }
        if (senha != confirmar) {
            toast("As senhas não conferem.")
            return false
        }
        true
    }

    // =====================================================
    // 🚀 Cadastrar administrador
    // =====================================================
    private fun cadastrarAdmin() = with(binding) {
        if (!validateAll()) return@with

        val email = edtEmail.text.toString().trim().lowercase()
        val senha = edtPassword.text.toString()

        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val result = repo.createAdmin(email, senha)
            withContext(Dispatchers.Main) {
                setLoading(false)
                when (result) {
                    is AppResult.Success -> {
                        toast("Administrador cadastrado com sucesso!")
                        Navigator.toLogin(this@AdminRegistrationFragment)
                    }

                    is AppResult.Error -> {
                        toast(result.throwable.message ?: "Erro ao cadastrar")
                    }
                }
            }
        }
    }

    // =====================================================
    // ⚙️ Controle de loading
    // =====================================================
    private fun setLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
        edtEmail.isEnabled = !loading
        edtPassword.isEnabled = !loading
        edtConfirmPassword.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
