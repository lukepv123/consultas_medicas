package curso.petenusso.clinicsapp.ui.medico.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import curso.petenusso.clinicsapp.databinding.FragmentProntuarioDialogBinding
import curso.petenusso.clinicsapp.model.prontuario.Prontuario

class ProntuarioDialogFragment(
    private val prontuarios: List<Prontuario>
) : DialogFragment() {

    private var _binding: FragmentProntuarioDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentProntuarioDialogBinding.inflate(layoutInflater)

        val adapter = ProntuarioAdapter(prontuarios)
        binding.recyclerProntuarios.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProntuarios.adapter = adapter

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Prontuários do Paciente")
            .setPositiveButton("Voltar") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
