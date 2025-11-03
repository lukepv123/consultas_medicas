package curso.petenusso.clinicsapp.ui.paciente.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import curso.petenusso.clinicsapp.api.consulta.dto.CreateConsultaRequest
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.consultas.ConsultaRepository
import curso.petenusso.clinicsapp.data.medico.MedicoRepository
import curso.petenusso.clinicsapp.databinding.FragmentAgendarConsultaPacienteBinding
import curso.petenusso.clinicsapp.model.medico.Especialidade
import curso.petenusso.clinicsapp.model.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AgendarConsultaPacienteFragment : Fragment() {

    private var _binding: FragmentAgendarConsultaPacienteBinding? = null
    private val binding get() = _binding!!

    // ✅ Substituindo Retrofit direto por repositories
    private val medicoRepo = MedicoRepository()
    private val consultaRepo = ConsultaRepository()

    private var dataSelecionada: Calendar? = null
    private var medicoSelecionado: curso.petenusso.clinicsapp.api.medico.dto.MedicoDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgendarConsultaPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnReturn.setOnClickListener {
            Navigator.goBack(this@AgendarConsultaPacienteFragment)
        }

        binding.btnSelecionarData.setOnClickListener { abrirDatePicker() }

        configurarSpinners()
        binding.btnConfirmar.setOnClickListener { confirmarAgendamento() }
    }

    // ======================================================
    // 🩺 Inicializa spinners
    // ======================================================
    private fun configurarSpinners() {
        // Horários
        val horarios = gerarHorarios()
        binding.spHora.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            horarios
        )

        // Especialidades (do enum)
        val especialidades = Especialidade.values().map { it.name }
        binding.spEspecialidade.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            especialidades
        )

        // Listener: muda especialidade → recarrega médicos
        binding.spEspecialidade.setOnItemSelectedListenerCompat<String> { especialidade ->
            carregarMedicosPorEspecialidade(especialidade)
        }
    }

    // ======================================================
    // 📅 Selecionar data
    // ======================================================
    private fun abrirDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a data da consulta")
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calUtc.timeInMillis = selection

            val calLocal = Calendar.getInstance()
            calLocal.set(
                calUtc.get(Calendar.YEAR),
                calUtc.get(Calendar.MONTH),
                calUtc.get(Calendar.DAY_OF_MONTH)
            )

            val diaSemana = calLocal.get(Calendar.DAY_OF_WEEK)
            if (diaSemana == Calendar.SATURDAY || diaSemana == Calendar.SUNDAY) {
                Snackbar.make(binding.root, "⚠️ Consultas não disponíveis em fins de semana", Snackbar.LENGTH_SHORT).show()
                return@addOnPositiveButtonClickListener
            }

            dataSelecionada = calLocal
            binding.btnSelecionarData.text =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calLocal.time)
        }

        picker.show(parentFragmentManager, "datePicker")
    }

    // ======================================================
    // 👩‍⚕️ Buscar médicos da especialidade (usando repository)
    // ======================================================
    private fun carregarMedicosPorEspecialidade(especialidade: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                when (val result = medicoRepo.listar()) {
                    is AppResult.Success -> {
                        val medicosFiltrados = result.data.data.filter {
                            it.especialidade.equals(especialidade, ignoreCase = true)
                        }

                        withContext(Dispatchers.Main) {
                            if (medicosFiltrados.isEmpty()) {
                                Snackbar.make(binding.root, "Nenhum médico encontrado para $especialidade", Snackbar.LENGTH_LONG).show()
                                binding.spMedico.adapter = null
                                medicoSelecionado = null
                                return@withContext
                            }

                            val nomes = medicosFiltrados.map { "${it.nome} (${it.crm})" }
                            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nomes)
                            binding.spMedico.adapter = adapter

                            binding.spMedico.setOnItemSelectedListenerCompat<String> {
                                val pos = binding.spMedico.selectedItemPosition
                                medicoSelecionado = medicosFiltrados[pos]
                            }
                        }
                    }

                    is AppResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "Erro ao carregar médicos: ${result.throwable.localizedMessage}", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Erro inesperado: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // ======================================================
    // ✅ Confirmar agendamento (sem mudar a lógica)
    // ======================================================
    private fun confirmarAgendamento() {
        val data = dataSelecionada
        val hora = binding.spHora.selectedItem?.toString()
        val medico = medicoSelecionado
        val paciente = SessionManager.asPaciente()

        if (data == null || hora == null || medico == null || paciente?.pacienteId == null) {
            Snackbar.make(binding.root, "Selecione todos os campos antes de confirmar", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dataHora = "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(data.time)}T$hora:00-03:00"

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Agendamento")
            .setMessage("Deseja agendar a consulta em ${SimpleDateFormat("dd/MM/yyyy").format(data.time)} às $hora com Dr(a). ${medico.nome}?")
            .setPositiveButton("Sim") { _, _ ->
                cadastrarConsulta(dataHora, medico.id, paciente.pacienteId)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    // ======================================================
    // 📤 Cadastro da consulta (via repository)
    // ======================================================
    private fun cadastrarConsulta(dataHora: String, idMedico: String, idPaciente: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (val result = consultaRepo.cadastrar(CreateConsultaRequest(dataHora, idMedico, idPaciente))) {
                is AppResult.Success -> {
                    withContext(Dispatchers.Main) {
                        when (result.data) {
                            201 -> Snackbar.make(binding.root, "✅ Consulta cadastrada com sucesso!", Snackbar.LENGTH_LONG).show()
                            409 -> Snackbar.make(binding.root, "⚠️ Horário indisponível, escolha outro.", Snackbar.LENGTH_LONG).show()
                            422 -> Snackbar.make(binding.root, "⚠️ Data/hora inválida.", Snackbar.LENGTH_LONG).show()
                            401, 403 -> Snackbar.make(binding.root, "Sessão expirada, faça login novamente.", Snackbar.LENGTH_LONG).show()
                            else -> Snackbar.make(binding.root, "❌ Horário ou data indisponíveis (${result.data})", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }

                is AppResult.Error -> {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, "Erro ao cadastrar consulta: ${result.throwable.localizedMessage}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /** Gera uma lista de horários de 20 em 20 minutos entre 08:00 e 18:00 */
    private fun gerarHorarios(): List<String> {
        val lista = mutableListOf<String>()
        var hora = 8
        var minuto = 0
        while (hora < 18 || (hora == 18 && minuto == 0)) {
            lista.add(String.format("%02d:%02d", hora, minuto))
            minuto += 20
            if (minuto >= 60) {
                minuto = 0
                hora++
            }
        }
        return lista
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

/** Simplifica listener de Spinner tipado */
fun <T> android.widget.Spinner.setOnItemSelectedListenerCompat(onSelect: (T) -> Unit) {
    onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
            val item = parent?.getItemAtPosition(position) as T
            onSelect(item)
        }
        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }
}
