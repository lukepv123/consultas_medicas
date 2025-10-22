package curso.petenusso.clinicsapp.ui.paciente.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.FragmentLobbyPacienteBinding
import curso.petenusso.clinicsapp.ui.paciente.PacienteActivity

class LobbyPacienteFragment : Fragment() {

    private var _binding: FragmentLobbyPacienteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyPacienteBinding.inflate(inflater, container, false)

        setupListeners()

        return binding.root
    }

    private fun setupListeners() = with(binding) {
        btnAgendar.setOnClickListener {
            // Navegação interna: continua na mesma Activity -> NÃO limpa sessão
            Navigator.showAgendarConsultaPaciente(this@LobbyPacienteFragment)
        }

        btnConsultar.setOnClickListener {
            // Navegação interna: mesma Activity
            Navigator.showMinhasConsultasPaciente(this@LobbyPacienteFragment)
        }

        btnDadosPessoais.setOnClickListener {
            // Navegação interna: mesma Activity
            Navigator.showDadosPessoaisPaciente(this@LobbyPacienteFragment)
        }

        btnLogout.setOnClickListener {
            // Logout explícito: pede para a Activity limpar sessão e sair
            (requireActivity() as? PacienteActivity)?.logoutToLogin()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // apenas limpa o binding; sessão NÃO é tocada aqui
    }
}
