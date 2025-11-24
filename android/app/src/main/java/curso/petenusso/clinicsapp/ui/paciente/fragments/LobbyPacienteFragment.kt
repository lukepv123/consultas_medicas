package curso.petenusso.clinicsapp.ui.paciente.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Nada de notificação aqui, só UI
    }

    private fun setupListeners() = with(binding) {
        btnAgendar.setOnClickListener {
            Navigator.showAgendarConsultaPaciente(this@LobbyPacienteFragment)
        }

        btnConsultar.setOnClickListener {
            Navigator.showMinhasConsultasPaciente(this@LobbyPacienteFragment)
        }

        btnDadosPessoais.setOnClickListener {
            Navigator.showDadosPessoaisPaciente(this@LobbyPacienteFragment)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            (requireActivity() as? PacienteActivity)?.logoutToLogin()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
