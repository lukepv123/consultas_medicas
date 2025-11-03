package curso.petenusso.clinicsapp.ui.adm.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.api.consulta.dto.CancelarConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaResumoDTO
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.consultas.ConsultaRepository
import curso.petenusso.clinicsapp.data.paciente.PacienteRepository
import curso.petenusso.clinicsapp.databinding.FragmentCancelarConsultaAdmBinding
import kotlinx.coroutines.launch

class CancelarConsultaAdmFragment : Fragment() {
    private var _binding: FragmentCancelarConsultaAdmBinding? = null
    private val binding get() = _binding!!

    // ✅ Agora só usa os repositórios
    private val pacienteRepo by lazy { PacienteRepository() }
    private val consultaRepo by lazy { ConsultaRepository() }

    private var consultasCarregadas: List<ConsultaResumoDTO> = emptyList()
    private val CPF_DIGITS_ONLY = Regex("^[0-9]{11}$")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCancelarConsultaAdmBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnReturn.setOnClickListener { Navigator.backToAdminLobby(this) }
        binding.btnBuscarConsultas.setOnClickListener { carregarConsultasPorCpf() }
        binding.btnCancelar.setOnClickListener { confirmarCancelamento() }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnBuscarConsultas.isEnabled = !loading
        binding.btnCancelar.isEnabled = !loading
        binding.btnReturn.isEnabled = !loading
        binding.edtCpf.isEnabled = !loading
        binding.edtJustificativa.isEnabled = !loading
        binding.spConsultas.isEnabled = !loading
    }

    /** Busca consultas futuras do paciente (status AGENDADA) */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarConsultasPorCpf() {
        val cpf = binding.edtCpf.text.toString().trim()
        if (!CPF_DIGITS_ONLY.matches(cpf)) {
            toast("Informe um CPF com 11 dígitos (somente números).")
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 🔹 1️⃣ Buscar ID do paciente via repository
                when (val idResult = pacienteRepo.buscarIdPorCpf(cpf)) {
                    is AppResult.Success -> {
                        val pacienteId = idResult.data

                        // 🔹 2️⃣ Listar consultas futuras via repository
                        when (val resp = consultaRepo.listarFuturasPorPaciente(pacienteId)) {
                            is AppResult.Success -> {
                                val todas = resp.data
                                val filtradas = todas.filter { it.status.equals("AGENDADA", ignoreCase = true) }
                                consultasCarregadas = filtradas

                                if (filtradas.isEmpty()) {
                                    setLoading(false)
                                    binding.spConsultas.adapter = null
                                    toast("Nenhuma consulta AGENDADA encontrada para este paciente.")
                                    return@launch
                                }

                                val itensVisuais = filtradas.map { c ->
                                    val quando = c.dataHoraConsulta?.let(::formatIsoToLocal) ?: "—"
                                    "$quando [AGENDADA]"
                                }

                                binding.spConsultas.adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_spinner_dropdown_item,
                                    itensVisuais
                                )
                                setLoading(false)
                            }

                            is AppResult.Error -> {
                                setLoading(false)
                                toast("Erro ao buscar consultas (${resp.throwable.message}).")
                            }
                        }
                    }

                    is AppResult.Error -> {
                        setLoading(false)
                        toast(idResult.throwable.message ?: "Erro ao buscar paciente.")
                    }
                }

            } catch (t: Throwable) {
                setLoading(false)
                toast("Erro de rede: ${t.message}")
            }
        }
    }

    /** Exibe confirmação antes de cancelar */
    private fun confirmarCancelamento() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar cancelamento")
            .setMessage("Deseja realmente cancelar esta consulta?")
            .setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                cancelarConsultaSelecionada()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /** Cancela a consulta selecionada com base no ID */
    private fun cancelarConsultaSelecionada() {
        val cpf = binding.edtCpf.text.toString().trim()
        if (!CPF_DIGITS_ONLY.matches(cpf)) {
            toast("Informe um CPF válido (11 dígitos).")
            return
        }

        val pos = binding.spConsultas.selectedItemPosition
        if (pos < 0 || consultasCarregadas.isEmpty()) {
            toast("Selecione uma consulta para cancelar.")
            return
        }

        val justificativa = binding.edtJustificativa.text.toString().trim()
        if (justificativa.length < 5) {
            toast("Informe uma justificativa válida (mínimo 5 caracteres).")
            return
        }

        val selecionada = consultasCarregadas[pos]
        val idConsulta = selecionada.id
        val dataHora = selecionada.dataHoraConsulta

        if (idConsulta.isNullOrBlank() || dataHora.isNullOrBlank()) {
            toast("Erro ao identificar a consulta selecionada.")
            return
        }

        val body = CancelarConsultaDTO(
            idConsulta = idConsulta,
            cpfPaciente = cpf,
            dataHoraConsulta = dataHora,
            justificativa = justificativa
        )

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = consultaRepo.cancelar(body)
                setLoading(false)
                if (resp is AppResult.Success && resp.data in listOf(200, 204)) {
                    toast("Consulta cancelada com sucesso.")
                    Navigator.backToAdminLobby(this@CancelarConsultaAdmFragment)
                } else {
                    toast("Erro ao cancelar (${(resp as? AppResult.Success)?.data ?: "?"}).")
                }
            } catch (t: Throwable) {
                setLoading(false)
                toast("Erro de rede: ${t.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatIsoToLocal(iso: String): String = try {
        java.time.OffsetDateTime.parse(iso)
            .atZoneSameInstant(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (_: Throwable) {
        iso
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
