package curso.petenusso.clinicsapp.ui.medico.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.consulta.ConsultaApi
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.paciente.PacienteRepository
import curso.petenusso.clinicsapp.data.prontuario.ProntuarioRepository
import curso.petenusso.clinicsapp.databinding.FragmentLobbyMedicoBinding
import curso.petenusso.clinicsapp.model.prontuario.Prontuario
import curso.petenusso.clinicsapp.model.session.SessionManager
import curso.petenusso.clinicsapp.ui.medico.dialogs.ProntuarioDialogFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LobbyMedicoFragment : Fragment() {

    private var _binding: FragmentLobbyMedicoBinding? = null
    private val binding get() = _binding!!

    // APIs diretas e repositórios existentes
    private val consultaApi = RetrofitFactory.retrofit().create(ConsultaApi::class.java)
    private val pacienteRepo = PacienteRepository()
    private val prontuarioRepo = ProntuarioRepository()

    private val consultas = mutableListOf<Pair<String, ConsultaItem>>() // (textoExibido, consulta)
    private var consultaSelecionada: ConsultaItem? = null
    private var nomePacienteSelecionado: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyMedicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarConsultasDoDia()
        setupButtons()
    }

    // ====================================================
    // 🔹 CONSULTAS DO MÉDICO LOGADO (usa ConsultaApi diretamente)
    // ====================================================
    private fun carregarConsultasDoDia() {
        val medicoId = SessionManager.current?.userId ?: return

        lifecycleScope.launch {
            try {
                binding.textPaciente.text = "Carregando consultas..."
                binding.spinnerConsultas.isEnabled = false

                val response = consultaApi.listarFuturasMedico(medicoId)
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!.data

                    if (lista.isEmpty()) {
                        binding.textPaciente.text = "Nenhuma consulta agendada para hoje."
                        return@launch
                    }

                    consultas.clear()

                    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val formatoSaida = SimpleDateFormat("HH:mm", Locale.getDefault())

                    for (consulta in lista) {
                        val dataHora = try {
                            formatoEntrada.parse(consulta.dataHoraConsulta)
                        } catch (_: Exception) {
                            null
                        }
                        val hora = dataHora?.let { formatoSaida.format(it) } ?: "?"
                        consultas.add(
                            "$hora - Paciente ${consulta.idPaciente.take(6)}..." to
                                    ConsultaItem(consulta.id, consulta.idPaciente, consulta.dataHoraConsulta)
                        )
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        consultas.map { it.first }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerConsultas.adapter = adapter
                    binding.spinnerConsultas.isEnabled = true

                    binding.spinnerConsultas.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                consultaSelecionada = consultas[position].second
                                buscarNomePaciente(consultaSelecionada!!.idPaciente)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                consultaSelecionada = null
                                binding.textPaciente.text = "Selecione uma consulta"
                            }
                        }
                } else {
                    binding.textPaciente.text =
                        "Erro ao buscar consultas: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                binding.textPaciente.text = "Erro: ${e.message}"
            }
        }
    }

    // ====================================================
    // 🔹 BUSCAR NOME DO PACIENTE PELO ID
    // ====================================================
    private fun buscarNomePaciente(idPaciente: String) {
        lifecycleScope.launch {
            binding.textPaciente.text = "Buscando paciente..."
            when (val result = pacienteRepo.buscarPaciente(idPaciente)) {
                is AppResult.Success -> {
                    val paciente = result.data
                    nomePacienteSelecionado = paciente.nome
                    binding.textPaciente.text = paciente.nome
                }
                is AppResult.Error -> {
                    binding.textPaciente.text = "Erro: ${result.throwable.message}"
                }
            }
        }
    }

    // ====================================================
    // 🔹 BUSCAR E EXIBIR PRONTUÁRIOS DO PACIENTE
    // ====================================================
    private fun showProntuarioDialog() {
        val pacienteId = consultaSelecionada?.idPaciente ?: return

        lifecycleScope.launch {
            when (val result = prontuarioRepo.listarPorPaciente(pacienteId)) {
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Prontuário do Paciente")
                            .setMessage("Nenhum prontuário encontrado.")
                            .setPositiveButton("Voltar", null)
                            .show()
                    } else {
                        val prontuarios = result.data.map {
                            Prontuario(
                                id = it.id,
                                idPaciente = it.idPaciente,
                                atendimento = it.atendimento,
                                alergias = it.alergias ?: "",
                                deficiencia = it.deficiencia ?: "",
                                comorbidade = it.comorbidade ?: "",
                                exames = it.exames ?: "",
                                medicacao = it.medicacao ?: "",
                                dataCadastro = it.dataCadastro
                            )
                        }
                        ProntuarioDialogFragment(prontuarios)
                            .show(parentFragmentManager, "dialogProntuario")
                    }
                }
                is AppResult.Error -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Erro")
                        .setMessage("Falha ao buscar prontuário: ${result.throwable.message}")
                        .setPositiveButton("Ok", null)
                        .show()
                }
            }
        }
    }

    // ====================================================
    // 🔹 BOTÕES
    // ====================================================
    private fun setupButtons() {
        binding.btnProntuario.setOnClickListener {
            if (consultaSelecionada == null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Atenção")
                    .setMessage("Selecione uma consulta primeiro.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                showProntuarioDialog()
            }
        }

        binding.btnRealizarConsulta.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Abrir consulta")
                .setMessage("Aqui abrirá o fragmento de realização da consulta.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.clear()
            Navigator.logoutToLogin(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ====================================================
    // 🔹 DATA CLASS LOCAL
    // ====================================================
    data class ConsultaItem(
        val id: String,
        val idPaciente: String,
        val dataHoraConsulta: String
    )
}
