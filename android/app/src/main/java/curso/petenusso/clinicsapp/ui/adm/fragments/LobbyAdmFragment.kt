package curso.petenusso.clinicsapp.ui.adm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.FragmentLobbyAdmBinding
import curso.petenusso.clinicsapp.model.session.SessionManager

class LobbyAdmFragment : Fragment() {
    private var _binding: FragmentLobbyAdmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLobbyAdmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnCadastrarMedico.setOnClickListener { Navigator.toAdminCadastrarMedico(this) }
        binding.btnCancelarConsulta.setOnClickListener { Navigator.toAdminCancelarConsulta(this) }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SessionManager.clear()
            Navigator.logoutToLogin(requireContext())
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
