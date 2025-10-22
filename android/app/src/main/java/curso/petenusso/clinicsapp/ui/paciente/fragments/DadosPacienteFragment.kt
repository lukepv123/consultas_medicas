package curso.petenusso.clinicsapp.ui.paciente.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.FragmentDadosPacienteBinding
import curso.petenusso.clinicsapp.model.session.SessionManager

class DadosPacienteFragment : Fragment() {

    private var _binding: FragmentDadosPacienteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDadosPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() = with(binding) {
        // Botão voltar
        btnReturn.setOnClickListener {
            Navigator.goBack(this@DadosPacienteFragment)
        }

        // Recupera dados do paciente logado
        val paciente = SessionManager.asPaciente()
        tvCpf.text = "CPF: ${paciente?.cpf ?: "-"}"
        tvNome.text = "Nome: ${paciente?.nome ?: "-"}"
        tvEmail.text = "Email: ${paciente?.emailOrUser ?: "-"}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
