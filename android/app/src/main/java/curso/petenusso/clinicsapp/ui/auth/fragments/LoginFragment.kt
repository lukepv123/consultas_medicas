package curso.petenusso.clinicsapp.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.adm.AdminRepository
import curso.petenusso.clinicsapp.data.firebase.AdmFirebaseRepository
import curso.petenusso.clinicsapp.databinding.FragmentLoginBinding
import curso.petenusso.clinicsapp.model.session.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // ✅ Usa apenas o repositório (sem Retrofit direto)
    private val adminRepo by lazy { AdminRepository() }

    private val adminRepoFirebase by lazy { AdmFirebaseRepository() }

    // Regex para validar e-mails em minúsculas
    private val EMAIL_LOWER_REGEX = Regex("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener { realizarLogin() }
        binding.btnCriarConta.setOnClickListener { Navigator.toCriarPaciente(this) }
    }

    // ==========================================================
    // =============== 🚀 Lógica de login =======================
    // ==========================================================
    private fun realizarLogin() {
        val emailRaw = binding.edtUser.text?.toString()?.trim().orEmpty()
        val senha = binding.edtPassword.text?.toString().orEmpty()

        // 1️⃣ Validação de minúsculas
        if (emailRaw != emailRaw.lowercase()) {
            binding.edtUser.error = "Use apenas letras minúsculas"
            toast("E-mail deve estar todo em minúsculas.")
            return
        }

        // 2️⃣ Validação de formato
        if (!EMAIL_LOWER_REGEX.matches(emailRaw)) {
            binding.edtUser.error = "E-mail inválido (somente minúsculas)"
            toast("E-mail inválido. Use apenas minúsculas.")
            return
        }

        // 3️⃣ Senha obrigatória
        if (senha.isBlank()) {
            binding.edtPassword.error = "Informe a senha"
            toast("Por favor, informe sua senha.")
            return
        }

        // 4️⃣ Chamada ao repositório
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = adminRepoFirebase.login(emailRaw, senha)) {
                is AppResult.Success -> {
                    setLoading(false)
                    SessionManager.set(result.data)
                    Navigator.goToRole(requireContext(), result.data, finishCurrent = true)
                }

                is AppResult.Error -> {
                    setLoading(false)
                    handleLoginError(result.throwable)
                }
            }
        }
    }

    // ==========================================================
    // =============== 🔍 Tratamento de erros ===================
    // ==========================================================
    private fun handleLoginError(throwable: Throwable) {
        when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    401 -> toast("E-mail ou senha incorretos. Verifique suas credenciais e tente novamente.")
                    403 -> toast("Acesso negado. Sua conta não tem permissão para entrar.")
                    500 -> toast("Erro interno no servidor. Tente novamente em instantes.")
                    else -> toast("Falha ao entrar: ${throwable.message() ?: "Erro desconhecido."}")
                }
            }

            else -> toast("Erro de conexão: ${throwable.localizedMessage ?: "verifique sua internet."}")
        }
    }

    // ==========================================================
    // =============== 🔄 Controle de loading ===================
    // ==========================================================
    private fun setLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        btnCriarConta.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
