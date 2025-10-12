package curso.petenusso.clinicsapp.ui.adm.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.consulta.ConsultaApi
import curso.petenusso.clinicsapp.api.consulta.dto.CancelarConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaResumoDTO
import curso.petenusso.clinicsapp.api.pacientes.PacienteApi
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.databinding.FragmentCancelarConsultaAdmBinding
import kotlinx.coroutines.launch

class CancelarConsultaAdmFragment : Fragment() {
    private var _binding: FragmentCancelarConsultaAdmBinding? = null
    private val binding get() = _binding!!

    private val pacienteApi by lazy { RetrofitFactory.retrofit().create(PacienteApi::class.java) }
    private val consultaApi by lazy { RetrofitFactory.retrofit().create(ConsultaApi::class.java) }

    // cache da lista (apenas AGENDADA) para recuperar a seleção
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
        binding.btnCancelar.setOnClickListener { cancelarConsultaSelecionada() }
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

    /** Fluxo: CPF -> ID -> listar futuras por ID (apenas AGENDADA) */
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
                // 1) buscar ID por CPF
                val idResp = pacienteApi.buscarIdPorCpf(cpf)
                if (!idResp.isSuccessful) {
                    setLoading(false)
                    val err = idResp.errorBody()?.string()
                    toast(err?.takeIf { it.isNotBlank() } ?: "Não foi possível obter o ID do paciente (${idResp.code()}).")
                    return@launch
                }
                val pacienteId = idResp.body()?.id ?: run {
                    setLoading(false)
                    toast("Resposta inválida ao buscar ID do paciente.")
                    return@launch
                }

                // 2) listar futuras por ID
                val resp = consultaApi.listarFuturasPorPacienteId(idPaciente = pacienteId, page = 1, perPage = 50)
                if (resp.isSuccessful) {
                    val envelope = resp.body()
                    val todas = envelope?.list().orEmpty()

                    // 🔎 filtro local: somente AGENDADA
                    val filtradas = todas.filter { it.status?.equals("AGENDADA", ignoreCase = true) == true }
                    consultasCarregadas = filtradas

                    if (filtradas.isEmpty()) {
                        binding.spConsultas.adapter = null
                        setLoading(false)
                        toast("Nenhuma consulta AGENDADA encontrada para este paciente.")
                        return@launch
                    }

                    val itensVisuais = filtradas.map { c ->
                        val quando = c.dataHoraConsulta?.let(::formatIsoToLocal) ?: "—"
                        // como todas são AGENDADA, opcional mostrar o status
                        "$quando [AGENDADA]"
                    }

                    binding.spConsultas.adapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, itensVisuais)

                    setLoading(false)
                } else {
                    val err = resp.errorBody()?.string()
                    setLoading(false)
                    toast(err?.takeIf { it.isNotBlank() } ?: "Falha ao buscar consultas (${resp.code()}).")
                }
            } catch (t: Throwable) {
                setLoading(false)
                toast("Erro de rede: ${t.message}")
            }
        }
    }

    /** POST /consultas/cancelamento — enviando CPF e dataHoraConsulta */
    private fun cancelarConsultaSelecionada() {
        val cpf = binding.edtCpf.text.toString().trim()
        if (!CPF_DIGITS_ONLY.matches(cpf)) {
            toast("Informe um CPF com 11 dígitos (somente números).")
            return
        }

        val adapter = binding.spConsultas.adapter
        if (adapter == null || adapter.count == 0 || consultasCarregadas.isEmpty()) {
            toast("Selecione uma consulta.")
            return
        }

        val justificativa = binding.edtJustificativa.text.toString().trim()
        if (justificativa.length < 5) {
            toast("Informe a justificativa (mín. 5 caracteres).")
            return
        }

        val pos = binding.spConsultas.selectedItemPosition
        if (pos < 0 || pos >= consultasCarregadas.size) {
            toast("Seleção inválida.")
            return
        }

        val selecionada = consultasCarregadas[pos]
        val dataHora = selecionada.dataHoraConsulta
        if (dataHora.isNullOrBlank()) {
            toast("Não foi possível identificar a data/hora da consulta selecionada.")
            return
        }

        val body = CancelarConsultaDTO(
            cpfPaciente = cpf,
            dataHoraConsulta = dataHora, // envia exatamente o ISO do GET
            justificativa = justificativa
        )

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = consultaApi.cancelar(body)
                setLoading(false)
                if (resp.isSuccessful) {
                    toast("Consulta cancelada com sucesso.")
                    Navigator.backToAdminLobby(this@CancelarConsultaAdmFragment)
                } else {
                    val err = resp.errorBody()?.string()
                    toast(err?.takeIf { it.isNotBlank() } ?: "Falha ao cancelar (${resp.code()}).")
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

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
