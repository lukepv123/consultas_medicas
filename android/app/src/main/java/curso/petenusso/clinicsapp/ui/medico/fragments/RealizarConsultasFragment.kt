package curso.petenusso.clinicsapp.ui.medico.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import curso.petenusso.clinicsapp.api.prontuarios.dto.CreateProntuarioRequest
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.firebase.PacienteFirebaseRepository
import curso.petenusso.clinicsapp.data.firebase.ProntuarioFirebaseRepository
import curso.petenusso.clinicsapp.data.paciente.PacienteRepository
import curso.petenusso.clinicsapp.data.prontuario.ProntuarioRepository
import curso.petenusso.clinicsapp.databinding.FragmentRealizarConsultasBinding
import curso.petenusso.clinicsapp.model.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealizarConsultasFragment : Fragment() {

    private var _binding: FragmentRealizarConsultasBinding? = null
    private val binding get() = _binding!!

    private var pacienteId: String? = null
    private var consultaId: String? = null

    private val prontuarioRepo = ProntuarioRepository()
    private val pacienteRepo = PacienteRepository()
    private val pacienteFirebaseRepo by lazy { PacienteFirebaseRepository() }
    private val prontuarioFirebaseRepo by lazy { ProntuarioFirebaseRepository() }



    // guardamos o e-mail carregado do paciente
    private var pacienteEmail: String? = null
    private var pacienteNome: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pacienteId = it.getString("paciente_id")
            consultaId = it.getString("consulta_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRealizarConsultasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔙 Voltar ao lobby médico
        binding.btnReturn.setOnClickListener {
            Navigator.backToMedicoLobby(this)
        }

        // 🔹 Assim que o fragment é aberto, buscamos as infos do paciente
        if (!pacienteId.isNullOrBlank()) {
            lifecycleScope.launch {
                buscarInformacoesPaciente(pacienteId!!)
            }
        }

        // ✅ Concluir consulta → Cadastrar prontuário
        binding.btnConcluir.setOnClickListener {
            if (pacienteId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "ID do paciente não encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔴 Validação dos campos obrigatórios
            if (!areAllFieldsFilled()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alergias = binding.inputAlergias.text.toString()
            val deficiencia = binding.inputDeficiencia.text.toString()
            val comorbidade = binding.inputComorbidade.text.toString()
            val exames = binding.inputExames.text.toString()
            val medicacao = binding.inputMedicacao.text.toString()
            val atendimento = binding.inputAtendimento.text.toString()

            val request = CreateProntuarioRequest(
                idPaciente = pacienteId!!,
                atendimento = atendimento,
                alergias = alergias,
                deficiencia = deficiencia,
                comorbidade = comorbidade,
                exames = exames,
                medicacao = medicacao
            )

            lifecycleScope.launch {
                enviarProntuario(request)
            }
        }

        // 📧 Enviar prontuário por e-mail
        binding.btnEnviarEmail.setOnClickListener {
            if (pacienteEmail.isNullOrBlank()) {
                Toast.makeText(requireContext(), "E-mail do paciente não encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔴 Validação dos campos obrigatórios
            if (!areAllFieldsFilled()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val subject = "Prontuário da consulta"
            val message = """
        Olá ${pacienteNome ?: "paciente"},
        
        Segue o resumo do prontuário da sua consulta:

        Atendimento: ${binding.inputAtendimento.text}
        Alergias: ${binding.inputAlergias.text}
        Deficiência: ${binding.inputDeficiencia.text}
        Comorbidades: ${binding.inputComorbidade.text}
        Exames solicitados: ${binding.inputExames.text}
        Medicação: ${binding.inputMedicacao.text}

        Atenciosamente,
        Dr(a). ${SessionManager.asMedico()?.nome ?: "Médico(a)"}
    """.trimIndent()

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(pacienteEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "Enviar prontuário via..."))
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Nenhum aplicativo de e-mail encontrado", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // 🔹 Busca os dados do paciente pelo ID (usa PacienteRepository)
    private suspend fun buscarInformacoesPaciente(id: String) {
        withContext(Dispatchers.IO) {
            val result = pacienteFirebaseRepo.buscarPaciente(id)
            withContext(Dispatchers.Main) {
                when (result) {
                    is AppResult.Success -> {
                        val paciente = result.data
                        pacienteEmail = paciente.email
                        pacienteNome = paciente.nome

                        if (pacienteEmail.isNullOrBlank()) {
                            Toast.makeText(requireContext(), "Paciente não possui e-mail cadastrado", Toast.LENGTH_SHORT).show()
                        }
                    }

                    is AppResult.Error -> {
                        Toast.makeText(requireContext(), "Erro ao buscar paciente: ${result.throwable.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun enviarProntuario(request: CreateProntuarioRequest) {
        withContext(Dispatchers.IO) {
            val result = prontuarioFirebaseRepo.criarProntuario(request)

            withContext(Dispatchers.Main) {
                when (result) {
                    is AppResult.Success -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Consulta Concluída")
                            .setMessage("Prontuário criado com sucesso para o paciente!")
                            .setPositiveButton("OK") { _, _ ->
                                Navigator.backToMedicoLobby(this@RealizarConsultasFragment)
                            }
                            .show()
                    }

                    is AppResult.Error -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Erro ao criar prontuário")
                            .setMessage(result.throwable.message ?: "Erro desconhecido")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun areAllFieldsFilled(): Boolean {
        val alergias = binding.inputAlergias.text.toString().trim()
        val deficiencia = binding.inputDeficiencia.text.toString().trim()
        val comorbidade = binding.inputComorbidade.text.toString().trim()
        val exames = binding.inputExames.text.toString().trim()
        val medicacao = binding.inputMedicacao.text.toString().trim()
        val atendimento = binding.inputAtendimento.text.toString().trim()

        return alergias.isNotEmpty()
                && deficiencia.isNotEmpty()
                && comorbidade.isNotEmpty()
                && exames.isNotEmpty()
                && medicacao.isNotEmpty()
                && atendimento.isNotEmpty()
    }




    companion object {
        @JvmStatic
        fun newInstance(pacienteId: String, consultaId: String) =
            RealizarConsultasFragment().apply {
                arguments = Bundle().apply {
                    putString("paciente_id", pacienteId)
                    putString("consulta_id", consultaId)
                }
            }
    }
}
