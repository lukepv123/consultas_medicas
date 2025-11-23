package curso.petenusso.clinicsapp.ui.adm.fragments

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.firebase.MedicoFirebaseRepository
import curso.petenusso.clinicsapp.data.medico.MedicoRepository
import curso.petenusso.clinicsapp.databinding.FragmentCadastrarMedicoAdmBinding
import curso.petenusso.clinicsapp.model.medico.Especialidade
import curso.petenusso.clinicsapp.model.session.SessionManager
import kotlinx.coroutines.launch

class CadastrarMedicoAdmFragment : Fragment() {
    private var _binding: FragmentCadastrarMedicoAdmBinding? = null
    private val binding get() = _binding!!

    // ✅ Usa o repositório centralizado (sem criar Retrofit manualmente)
    private val repo by lazy { MedicoRepository() }
    private val repoFirebase by lazy { MedicoFirebaseRepository()}
    // ---- Regras de segurança ----
    private companion object Rules {
        const val CRM_MIN = 6
        const val CRM_MAX = 12
        const val NAME_MIN = 3
        const val NAME_MAX = 80
        const val PASS_MIN = 8
        const val PASS_MAX = 80
        const val EMAIL_MAX = 120
        val NAME_REGEX = Regex("^[A-Za-zÀ-ÖØ-öø-ÿ ]+$") // letras + acentos + espaço
        val PASS_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")
        val DIGITS_ONLY = Regex("^[0-9]+$")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCadastrarMedicoAdmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupEspecialidadeSpinner()
        applyInputFilters()
        setupLiveHints()

        binding.btnReturn.setOnClickListener { Navigator.backToAdminLobby(this) }
        binding.btnCadastrar.setOnClickListener { cadastrar() }
    }

    /** Spinner de especialidades */
    private fun setupEspecialidadeSpinner() {
        val items = Especialidade.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        binding.spEspecialidade.adapter = adapter
    }

    /** Filtros de entrada (segurança) */
    private fun applyInputFilters() {
        binding.edtCrm.filters = arrayOf(
            InputFilter.LengthFilter(CRM_MAX),
            RegexAllowFilter(DIGITS_ONLY)
        )

        binding.edtNome.filters = arrayOf(
            InputFilter.LengthFilter(NAME_MAX),
            RegexAllowFilter(NAME_REGEX)
        )

        binding.edtEmail.filters = arrayOf(InputFilter.LengthFilter(EMAIL_MAX))
        binding.edtSenha.filters = arrayOf(InputFilter.LengthFilter(PASS_MAX))
        binding.edtRepetirSenha.filters = arrayOf(InputFilter.LengthFilter(PASS_MAX))
    }

    /** Validação dinâmica ao digitar */
    private fun setupLiveHints() {
        binding.edtCrm.doAfterTextChanged {
            val crm = it?.toString()?.trim().orEmpty()
            if (crm.isNotEmpty() && (!crm.matches(DIGITS_ONLY) || crm.length !in CRM_MIN..CRM_MAX))
                binding.edtCrm.error = "CRM deve ter $CRM_MIN–$CRM_MAX dígitos"
            else binding.edtCrm.error = null
        }

        binding.edtNome.doAfterTextChanged {
            val nome = it?.toString()?.trim().orEmpty()
            if (nome.isNotEmpty() && (!nome.matches(NAME_REGEX) || nome.length !in NAME_MIN..NAME_MAX))
                binding.edtNome.error = "Somente letras e espaços ($NAME_MIN–$NAME_MAX)"
            else binding.edtNome.error = null
        }

        binding.edtEmail.doAfterTextChanged {
            val email = it?.toString()?.trim().orEmpty()
            if (email.isNotEmpty() && (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length > EMAIL_MAX))
                binding.edtEmail.error = "E-mail inválido"
            else binding.edtEmail.error = null
        }

        binding.edtSenha.doAfterTextChanged { validatePasswords(); animatePasswordIcon() }
        binding.edtRepetirSenha.doAfterTextChanged { validatePasswords(); animatePasswordIcon() }
    }

    /** Validação das senhas */
    private fun validatePasswords() {
        val senha = binding.edtSenha.text?.toString().orEmpty()
        val rep   = binding.edtRepetirSenha.text?.toString().orEmpty()

        when {
            senha.length !in PASS_MIN..PASS_MAX -> binding.edtSenha.error = "Senha $PASS_MIN–$PASS_MAX chars"
            !senha.matches(PASS_REGEX) -> binding.edtSenha.error = "Use maiús., minús., número e símbolo"
            else -> binding.edtSenha.error = null
        }

        if (rep.isNotEmpty() && rep != senha)
            binding.edtRepetirSenha.error = "Senhas não conferem"
        else
            binding.edtRepetirSenha.error = null
    }

    /** Valida tudo antes de enviar */
    private fun validateAll(): Boolean {
        val crm  = binding.edtCrm.text.toString().trim()
        val nome = binding.edtNome.text.toString().trim()
        val email= binding.edtEmail.text.toString().trim().lowercase()
        val senha= binding.edtSenha.text.toString()
        val rep  = binding.edtRepetirSenha.text.toString()

        if (!crm.matches(DIGITS_ONLY) || crm.length !in CRM_MIN..CRM_MAX) {
            toast("CRM deve ter $CRM_MIN–$CRM_MAX dígitos."); return false
        }
        if (!nome.matches(NAME_REGEX) || nome.length !in NAME_MIN..NAME_MAX) {
            toast("Nome inválido. Use letras e espaços ($NAME_MIN–$NAME_MAX)."); return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length > EMAIL_MAX) {
            toast("E-mail inválido."); return false
        }
        if (senha.length !in PASS_MIN..PASS_MAX || !senha.matches(PASS_REGEX)) {
            toast("Senha fraca. Use maiús., minús., número e símbolo."); return false
        }
        if (senha != rep) {
            toast("Senhas não conferem."); return false
        }
        if (binding.spEspecialidade.selectedItem == null) {
            toast("Selecione a especialidade."); return false
        }
        return true
    }

    /** Envio ao servidor */
    private fun cadastrar() {
        if (SessionManager.asAdm() == null) {
            toast("Acesso restrito ao administrador."); requireActivity().finish(); return
        }
        if (!validateAll()) return

        val crm  = binding.edtCrm.text.toString().trim()
        val nome = binding.edtNome.text.toString().trim()
        val email= binding.edtEmail.text.toString().trim().lowercase()
        val senha= binding.edtSenha.text.toString()
        val especialidade = binding.spEspecialidade.selectedItem?.toString() ?: ""

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val res = repoFirebase.criarComAccount(
                crm = crm,
                nome = nome,
                especialidadeEnumName = especialidade,
                email = email,
                senha = senha
            )) {
                is AppResult.Success -> {
                    setLoading(false)
                    toast("Médico cadastrado com sucesso!")
                    Navigator.backToAdminLobby(this@CadastrarMedicoAdmFragment)
                }
                is AppResult.Error -> {
                    setLoading(false)
                    toast("Erro ao cadastrar médico")
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCadastrar.isEnabled = !loading
        binding.btnReturn.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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

/** Filtro para permitir apenas caracteres válidos conforme regex */
class RegexAllowFilter(private val regex: Regex) : InputFilter {
    override fun filter(
        source: CharSequence?, start: Int, end: Int,
        dest: Spanned?, dstart: Int, dend: Int
    ): CharSequence? {
        val new = (dest?.substring(0, dstart).orEmpty()
                + (source?.subSequence(start, end) ?: "")
                + dest?.substring(dend, dest.length).orEmpty())
        return if (new.isEmpty() || regex.matches(new)) null else ""
    }
}
