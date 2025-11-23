package curso.petenusso.clinicsapp.ui.auth.fragments

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.api.dto.AccountDTO
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteCreateRequest
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.data.firebase.PacienteFirebaseRepository
import curso.petenusso.clinicsapp.data.paciente.PacienteRepository
import curso.petenusso.clinicsapp.databinding.FragmentCriarContaPacienteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CriarContaPacienteFragment : Fragment() {

    private var _binding: FragmentCriarContaPacienteBinding? = null
    private val binding get() = _binding!!

    // ✅ Agora usa apenas o repository
    private val pacienteRepo by lazy { PacienteRepository() }
    private val pacienteFirebaseRepo by lazy { PacienteFirebaseRepository() }
    // ---------- Regras ----------
    private companion object Rules {
        const val CPF_LENGTH = 11
        const val NAME_MIN = 3
        const val NAME_MAX = 80
        const val PASS_MIN = 8
        const val PASS_MAX = 80
        const val EMAIL_MAX = 120

        val DIGITS_ONLY = Regex("^[0-9]+$")
        val NAME_REGEX = Regex("^[A-Za-zÀ-ÖØ-öø-ÿ ]+$")
        val PASS_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCriarContaPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyInputFilters()
        setupLiveValidation()

        binding.btnReturn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCadastrar.setOnClickListener {
            cadastrarPaciente()
        }
    }

    // ============================================================
    // ================ FILTROS DE ENTRADA ========================
    // ============================================================
    private fun applyInputFilters() = with(binding) {
        edtCpf.filters = arrayOf(
            InputFilter.LengthFilter(CPF_LENGTH),
            RegexAllowFilter(DIGITS_ONLY)
        )
        edtNome.filters = arrayOf(
            InputFilter.LengthFilter(NAME_MAX),
            RegexAllowFilter(NAME_REGEX)
        )
        edtEmail.filters = arrayOf(InputFilter.LengthFilter(EMAIL_MAX))
        edtSenha.filters = arrayOf(InputFilter.LengthFilter(PASS_MAX))
        edtRepetirSenha.filters = arrayOf(InputFilter.LengthFilter(PASS_MAX))
    }

    // ============================================================
    // ================ VALIDAÇÃO DINÂMICA ========================
    // ============================================================
    private fun setupLiveValidation() = with(binding) {
        edtCpf.doAfterTextChanged {
            val cpf = it?.toString()?.trim().orEmpty()
            edtCpf.error = if (cpf.isNotEmpty() && (!cpf.matches(DIGITS_ONLY) || cpf.length != CPF_LENGTH))
                "CPF deve ter $CPF_LENGTH dígitos numéricos" else null
        }

        edtNome.doAfterTextChanged {
            val nome = it?.toString()?.trim().orEmpty()
            edtNome.error = if (nome.isNotEmpty() && (!nome.matches(NAME_REGEX) || nome.length !in NAME_MIN..NAME_MAX))
                "Somente letras e espaços ($NAME_MIN–$NAME_MAX)" else null
        }

        edtEmail.doAfterTextChanged {
            val email = it?.toString()?.trim().orEmpty()
            edtEmail.error = if (email.isNotEmpty() && (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length > EMAIL_MAX))
                "E-mail inválido" else null
        }

        edtSenha.doAfterTextChanged { validatePasswords(); animatePasswordIcon() }
        edtRepetirSenha.doAfterTextChanged { validatePasswords(); animatePasswordIcon() }
    }

    private fun validatePasswords() = with(binding) {
        val senha = edtSenha.text?.toString().orEmpty()
        val repetir = edtRepetirSenha.text?.toString().orEmpty()

        when {
            senha.length !in PASS_MIN..PASS_MAX ->
                edtSenha.error = "Senha $PASS_MIN–$PASS_MAX caracteres"
            !senha.matches(PASS_REGEX) ->
                edtSenha.error = "Use maiús., minús., número e símbolo"
            else -> edtSenha.error = null
        }

        edtRepetirSenha.error = if (repetir.isNotEmpty() && repetir != senha)
            "Senhas não conferem" else null
    }

    // ============================================================
    // ================ VALIDAÇÃO FINAL ===========================
    // ============================================================
    private fun validateAll(): Boolean = with(binding) {
        val cpf = edtCpf.text.toString().trim()
        val nome = edtNome.text.toString().trim()
        val email = edtEmail.text.toString().trim().lowercase()
        val senha = edtSenha.text.toString()
        val repetir = edtRepetirSenha.text.toString()

        if (!cpf.matches(DIGITS_ONLY) || cpf.length != CPF_LENGTH) {
            toast("CPF inválido. Deve ter $CPF_LENGTH dígitos.")
            return false
        }
        if (!nome.matches(NAME_REGEX) || nome.length !in NAME_MIN..NAME_MAX) {
            toast("Nome inválido. Use apenas letras e espaços ($NAME_MIN–$NAME_MAX).")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length > EMAIL_MAX) {
            toast("E-mail inválido.")
            return false
        }
        if (senha.length !in PASS_MIN..PASS_MAX || !senha.matches(PASS_REGEX)) {
            toast("Senha fraca. Use maiús., minús., número e símbolo.")
            return false
        }
        if (senha != repetir) {
            toast("Senhas não conferem.")
            return false
        }
        true
    }

    // ============================================================
    // ================ REQUISIÇÃO CENTRALIZADA ===================
    // ============================================================
    private fun cadastrarPaciente() = with(binding) {
        if (!validateAll()) return@with

        val dto = PacienteCreateRequest(
            cpf = edtCpf.text.toString().trim(),
            nome = edtNome.text.toString().trim(),
            account = AccountDTO(
                email = edtEmail.text.toString().trim().lowercase(),
                senha = edtSenha.text.toString().trim()
            )
        )

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val result = pacienteFirebaseRepo.cadastrar(dto)
            withContext(Dispatchers.Main) {
                setLoading(false)
                when (result) {
                    is AppResult.Success -> {
                        if (result.data == 201) {
                            toast("Cadastro realizado com sucesso!")
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        } else {
                            toast("Erro ao cadastrar (${result.data})")
                        }
                    }
                    is AppResult.Error -> {
                        toast("Erro: ${result.throwable.message ?: "Falha na requisição"}")
                    }
                }
            }
        }
    }

    // ============================================================
    // ================ UTILS =====================================
    // ============================================================
    private fun setLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnCadastrar.isEnabled = !loading
        btnReturn.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun animatePasswordIcon() = with(binding) {
        val senha = edtSenha.text?.toString().orEmpty()
        val repetir = edtRepetirSenha.text?.toString().orEmpty()

        ivPasswordMatchStatus.visibility = if (repetir.isNotEmpty()) View.VISIBLE else View.GONE
        if (repetir.isNotEmpty()) {
            val icon = if (senha == repetir) R.drawable.ic_check_green else R.drawable.ic_check_red
            ivPasswordMatchStatus.setImageResource(icon)
            ivPasswordMatchStatus.animate().alpha(1f).setDuration(250).start()
        }
    }
}

/** 🔹 Filtro genérico para restringir caracteres via Regex */
class RegexAllowFilter(private val regex: Regex) : InputFilter {
    override fun filter(
        source: CharSequence?, start: Int, end: Int,
        dest: Spanned?, dstart: Int, dend: Int
    ): CharSequence? {
        val newText = (dest?.substring(0, dstart).orEmpty()
                + (source?.subSequence(start, end) ?: "")
                + dest?.substring(dend, dest.length).orEmpty())
        return if (newText.isEmpty() || regex.matches(newText)) null else ""
    }
}
