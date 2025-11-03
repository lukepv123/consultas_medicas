package curso.petenusso.clinicsapp.ui.paciente.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.consultas.ConsultaRepository
import curso.petenusso.clinicsapp.data.medico.MedicoRepository
import curso.petenusso.clinicsapp.databinding.FragmentMinhasConsultasPacienteBinding
import curso.petenusso.clinicsapp.model.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class MinhasConsultasPacienteFragment : Fragment() {

    private var _binding: FragmentMinhasConsultasPacienteBinding? = null
    private val binding get() = _binding!!

    // ✅ Repositórios centralizados
    private val consultaRepo = ConsultaRepository()
    private val medicoRepo = MedicoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMinhasConsultasPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        carregarConsultas()
    }

    private fun setupUI() = with(binding) {
        btnReturn.setOnClickListener {
            Navigator.goBack(this@MinhasConsultasPacienteFragment)
        }
    }

    // ======================================================
    // 🔹 Carrega consultas e médicos
    // ======================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarConsultas() {
        val paciente = SessionManager.asPaciente() ?: return
        val idPaciente = paciente.pacienteId ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1️⃣ Busca consultas
                val futurasResult = consultaRepo.listarFuturas(idPaciente)
                val passadasResult = consultaRepo.listarPassadas(idPaciente)

                val futuras = if (futurasResult is AppResult.Success) futurasResult.data else emptyList()
                val passadas = if (passadasResult is AppResult.Success) passadasResult.data else emptyList()

                // 2️⃣ Busca lista de médicos uma única vez
                val medicosResult = medicoRepo.listar()
                val medicosMap = if (medicosResult is AppResult.Success) {
                    medicosResult.data.data.associateBy { it.id }
                } else emptyMap()

                withContext(Dispatchers.Main) {
                    if (futuras.isEmpty() && passadas.isEmpty()) {
                        Snackbar.make(binding.root, "Nenhuma consulta encontrada.", Snackbar.LENGTH_LONG).show()
                        return@withContext
                    }

                    val formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

                    val futurasFormatadas = futuras.map {
                        val dataLocal = OffsetDateTime.parse(it.dataHoraConsulta)
                            .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                        val medico = medicosMap[it.idMedico]
                        val nomeMedico = medico?.nome ?: "Desconhecido"
                        val especialidade = medico?.especialidade ?: "N/A"
                        "${dataLocal.format(formatador)} (${it.status}) — $nomeMedico ($especialidade)" to it.idMedico
                    }

                    val historicoFormatado = passadas.map {
                        val dataLocal = OffsetDateTime.parse(it.dataHoraConsulta)
                            .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                        val medico = medicosMap[it.idMedico]
                        val nomeMedico = medico?.nome ?: "Desconhecido"
                        val especialidade = medico?.especialidade ?: "N/A"
                        "${dataLocal.format(formatador)} (${it.status}) — $nomeMedico ($especialidade)" to it.idMedico
                    }

                    // 3️⃣ Preenche spinners
                    binding.spProximasConsultas.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        futurasFormatadas.map { it.first }
                    )

                    binding.spHistoricoConsultas.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        historicoFormatado.map { it.first }
                    )

                    // 4️⃣ Listeners com cache local de médicos
                    binding.spProximasConsultas.onItemSelectedListener =
                        criarListener(futurasFormatadas, binding.tvMedicoProxima, medicosMap)
                    binding.spHistoricoConsultas.onItemSelectedListener =
                        criarListener(historicoFormatado, binding.tvMedicoHistorico, medicosMap)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Erro ao carregar consultas: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // ======================================================
    // 🔹 Cria listener com cache de médicos
    // ======================================================
    private fun criarListener(
        lista: List<Pair<String, String>>,
        label: android.widget.TextView,
        medicosMap: Map<String, curso.petenusso.clinicsapp.api.medico.dto.MedicoDTO>
    ): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val idMedico = lista[position].second
                val medico = medicosMap[idMedico]
                label.text = if (medico != null) {
                    "👨‍⚕️ ${medico.nome} (${medico.crm}) — ${medico.especialidade.capitalize(Locale.ROOT)}"
                } else {
                    "Médico não encontrado"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
