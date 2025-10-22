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
import curso.petenusso.clinicsapp.api.RetrofitFactory
import curso.petenusso.clinicsapp.api.consulta.ConsultaApi
import curso.petenusso.clinicsapp.api.medico.MedicoApi
import curso.petenusso.clinicsapp.api.medico.dto.MedicoBasicResponse
import curso.petenusso.clinicsapp.core.Navigator
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

    private val consultaApi by lazy { RetrofitFactory.retrofit().create(ConsultaApi::class.java) }
    private val medicoApi by lazy { RetrofitFactory.retrofit().create(MedicoApi::class.java) }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarConsultas() {
        val paciente = SessionManager.asPaciente() ?: return
        val idPaciente = paciente.pacienteId ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val futuras = consultaApi.listarFuturas(idPaciente).body()?.data ?: emptyList()
                val passadas = consultaApi.listarPassadas(idPaciente).body()?.data ?: emptyList()

                withContext(Dispatchers.Main) {
                    if (futuras.isEmpty() && passadas.isEmpty()) {
                        Snackbar.make(binding.root, "Nenhuma consulta encontrada.", Snackbar.LENGTH_LONG).show()
                        return@withContext
                    }

                    val formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

                    // Preenche spinner de futuras
                    val futurasFormatadas = futuras.map {
                        val dataLocal = OffsetDateTime.parse(it.dataHoraConsulta)
                            .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                        "${dataLocal.format(formatador)} (${it.status})" to it.idMedico
                    }

                    val historicoFormatado = passadas.map {
                        val dataLocal = OffsetDateTime.parse(it.dataHoraConsulta)
                            .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                        "${dataLocal.format(formatador)} (${it.status})" to it.idMedico
                    }

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

                    // Listener para futuras
                    binding.spProximasConsultas.onItemSelectedListener =
                        criarListener(futurasFormatadas, binding.tvMedicoProxima)

                    // Listener para passadas
                    binding.spHistoricoConsultas.onItemSelectedListener =
                        criarListener(historicoFormatado, binding.tvMedicoHistorico)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Erro ao carregar consultas: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun criarListener(
        lista: List<Pair<String, String>>,
        label: android.widget.TextView
    ): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val idMedico = lista[position].second
                carregarDadosMedico(idMedico, label)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun carregarDadosMedico(idMedico: String, label: android.widget.TextView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = medicoApi.buscarBasico(idMedico)
                val medico: MedicoBasicResponse? = response.body()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && medico != null) {
                        label.text =
                            "👨‍⚕️ ${medico.nome} (${medico.crm}) — ${medico.especialidade.capitalize(Locale.ROOT)}"
                    } else {
                        label.text = "⚠️ Médico não encontrado"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    label.text = "Erro ao carregar médico: ${e.localizedMessage}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
