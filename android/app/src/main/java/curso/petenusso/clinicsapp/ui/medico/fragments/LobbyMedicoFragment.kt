package curso.petenusso.clinicsapp.ui.medico.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.consultas.ConsultaRepository
import curso.petenusso.clinicsapp.data.firebase.ConsultasFirebaseRepository
import curso.petenusso.clinicsapp.data.firebase.PacienteFirebaseRepository
import curso.petenusso.clinicsapp.data.firebase.ProntuarioFirebaseRepository
import curso.petenusso.clinicsapp.data.paciente.PacienteRepository
import curso.petenusso.clinicsapp.data.prontuario.ProntuarioRepository
import curso.petenusso.clinicsapp.databinding.FragmentLobbyMedicoBinding
import curso.petenusso.clinicsapp.model.prontuario.Prontuario
import curso.petenusso.clinicsapp.model.session.SessionManager
import curso.petenusso.clinicsapp.ui.medico.dialogs.ProntuarioDialogFragment
import kotlinx.coroutines.*
import java.time.*
import java.time.format.DateTimeFormatter

class LobbyMedicoFragment : Fragment() {

    private var _binding: FragmentLobbyMedicoBinding? = null
    private val binding get() = _binding!!

    // ====================================================
    // 🔹 REPOSITORIES (sem Retrofit direto)
    // ====================================================
    private val consultaRepo = ConsultaRepository()
    private val pacienteRepo = PacienteRepository()
    private val prontuarioRepo = ProntuarioRepository()

    private val pacienteFirebaseRepo by lazy { PacienteFirebaseRepository() }
    private val prontuarioFirebaseRepo by lazy { ProntuarioFirebaseRepository() }
    private val consultaFirebaseRepo by lazy { ConsultasFirebaseRepository() }

    private val consultas = mutableListOf<Pair<String, ConsultaItem>>()
    private var consultaSelecionada: ConsultaItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyMedicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        carregarConsultasDoMedico()
    }

    // ====================================================
    // 🔹 CONSULTAS DO MÉDICO LOGADO
    // ====================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarConsultasDoMedico() {
        val medicoSessao = SessionManager.asMedico()
        val medicoId = medicoSessao?.medicoId ?: medicoSessao?.userId

        if (medicoId.isNullOrBlank()) {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.textPaciente.text = "Erro: ID do médico não encontrado na sessão."
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.textPaciente.text = "Carregando consultas..."
                    binding.spinnerConsultas.isEnabled = false
                }

                Log.d("LobbyMedico", "🔹 Requisição: medicoId=$medicoId, email=${medicoSessao?.emailOrUser}")

                // ✅ Agora usa o repository (mesma lógica de resposta)
                val result = consultaFirebaseRepo.listarFuturasMedico(medicoId)

                when (result) {
                    is AppResult.Success -> {
                        val lista = result.data
                        Log.d("LobbyMedico", "✅ Consultas recebidas: ${lista.size}")

                        if (lista.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                binding.textPaciente.text = "Nenhuma consulta futura encontrada."
                            }
                            return@launch
                        }

                        consultas.clear()
                        val fuso = ZoneId.of("America/Sao_Paulo")
                        val formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

                        lista.sortedBy { it.dataHoraConsulta }.forEach { consulta ->
                            val texto = try {
                                val instante = Instant.parse(consulta.dataHoraConsulta)
                                val dataLocal = ZonedDateTime.ofInstant(instante, fuso)
                                "${dataLocal.format(formatador)} — Paciente ${consulta.idPaciente.take(6)}..."
                            } catch (e: Exception) {
                                "Data inválida — Paciente ${consulta.idPaciente.take(6)}..."
                            }

                            consultas.add(
                                texto to ConsultaItem(
                                    id = consulta.id,
                                    idPaciente = consulta.idPaciente,
                                    dataHoraConsulta = consulta.dataHoraConsulta
                                )
                            )
                        }

                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_dropdown_item,
                                consultas.map { it.first }
                            )
                            binding.spinnerConsultas.adapter = adapter
                            binding.spinnerConsultas.isEnabled = true
                            binding.textPaciente.text = "Selecione uma consulta"

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
                                        binding.textPaciente.text = "Selecione uma consulta"
                                    }
                                }

                            Toast.makeText(
                                requireContext(),
                                "Consultas carregadas: ${consultas.size}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is AppResult.Error -> {
                        withContext(Dispatchers.Main) {
                            binding.textPaciente.text =
                                "Erro ao buscar consultas: ${result.throwable.message}"
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("LobbyMedico", "❌ Erro ao carregar consultas", e)
                withContext(Dispatchers.Main) {
                    binding.textPaciente.text = "Erro: ${e.localizedMessage}"
                }
            }
        }
    }

    // ====================================================
    // 🔹 BUSCAR NOME DO PACIENTE
    // ====================================================
    private fun buscarNomePaciente(idPaciente: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.textPaciente.text = "Buscando paciente..."
                }

                val result = pacienteFirebaseRepo.buscarPaciente(idPaciente)

                withContext(Dispatchers.Main) {
                    when (result) {
                        is AppResult.Success -> {
                            val nome = result.data.nome ?: "Paciente sem nome"
                            Log.d("LobbyMedico", "✅ Paciente encontrado: $nome")
                            binding.textPaciente.text = nome
                        }

                        is AppResult.Error -> {
                            Log.e("LobbyMedico", "❌ Erro paciente: ${result.throwable.message}")
                            binding.textPaciente.text =
                                "Erro ao buscar paciente: ${result.throwable.message}"
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textPaciente.text = "Erro: ${e.localizedMessage}"
                }
                Log.e("LobbyMedico", "❌ Exceção ao buscar paciente", e)
            }
        }
    }

    // ====================================================
    // 🔹 EXIBIR PRONTUÁRIOS
    // ====================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showProntuarioDialog() {
        val pacienteId = consultaSelecionada?.idPaciente ?: return

        lifecycleScope.launch {
            when (val result = prontuarioFirebaseRepo.listarPorPaciente(pacienteId)) {
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Prontuário do Paciente")
                            .setMessage("Nenhum prontuário encontrado.")
                            .setPositiveButton("Voltar", null)
                            .show()
                    } else {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")
                        val zone = ZoneId.of("America/Sao_Paulo")

                        val prontuariosFormatados = result.data.map { dto ->
                            val dataFormatada = try {
                                val instant = Instant.parse(dto.dataCadastro)
                                val local = ZonedDateTime.ofInstant(instant, zone)
                                local.format(formatter)
                            } catch (e: Exception) {
                                dto.dataCadastro
                            }

                            Prontuario(
                                id = dto.id,
                                idPaciente = dto.idPaciente,
                                atendimento = dto.atendimento,
                                alergias = dto.alergias ?: "",
                                deficiencia = dto.deficiencia ?: "",
                                comorbidade = dto.comorbidade ?: "",
                                exames = dto.exames ?: "",
                                medicacao = dto.medicacao ?: "",
                                dataCadastro = dataFormatada
                            )
                        }

                        ProntuarioDialogFragment(prontuariosFormatados)
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupButtons() = with(binding) {
        btnProntuario.setOnClickListener {
            if (consultaSelecionada == null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Atenção")
                    .setMessage("Selecione uma consulta primeiro.")
                    .setPositiveButton("OK", null)
                    .show()
            } else showProntuarioDialog()
        }

        btnRealizarConsulta.setOnClickListener {
            if (consultaSelecionada == null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Atenção")
                    .setMessage("Selecione uma consulta para continuar.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            val pacienteId = consultaSelecionada!!.idPaciente
            val consultaId = consultaSelecionada!!.id
            Log.d("LobbyMedico", "➡️ Iniciando consulta para pacienteId=$pacienteId")

            Navigator.toRealizarConsulta(this@LobbyMedicoFragment, pacienteId, consultaId)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SessionManager.clear()
            Navigator.logoutToLogin(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class ConsultaItem(
        val id: String,
        val idPaciente: String,
        val dataHoraConsulta: String
    )
}
