package curso.petenusso.clinicsapp.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import curso.petenusso.clinicsapp.api.consulta.dto.CancelarConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaDTO
import curso.petenusso.clinicsapp.api.consulta.dto.ConsultaResumoDTO
import curso.petenusso.clinicsapp.api.consulta.dto.CreateConsultaRequest
import curso.petenusso.clinicsapp.core.AppResult
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ConsultasFirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val consultasCollection get() = firestore.collection("consultas")
    private val medicosCollection get() = firestore.collection("medicos")
    private val pacientesCollection get() = firestore.collection("pacientes")

    private companion object {
        private const val STATUS_AGENDADA = "AGENDADA"
        private const val STATUS_CANCELADA = "CANCELADA"
    }

    // =========================================================
    // LISTAGENS
    // =========================================================

    suspend fun listarFuturasPorPaciente(idPaciente: String): AppResult<List<ConsultaResumoDTO>> {
        return try {
            val pacienteRef = pacientesCollection.document(idPaciente)
            val snapshot = consultasCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .get()
                .await()

            val agora = Timestamp.now()

            val lista = snapshot.documents.mapNotNull { doc ->
                val dataHora = doc.get("data_hora") as? Timestamp
                val status = doc.getString("status") ?: STATUS_AGENDADA
                val refMedico = doc.get("ref_medico") as? DocumentReference
                val refPaciente = doc.get("ref_paciente") as? DocumentReference

                if (dataHora != null && dataHora > agora && status != STATUS_CANCELADA) {
                    val dto = mapToConsultaDTO(
                        id = doc.id,
                        dataHora = dataHora,
                        refMedico = refMedico,
                        refPaciente = refPaciente,
                        status = status
                    )
                    mapToResumo(dto)
                } else {
                    null
                }
            }.sortedBy { it.dataHoraConsulta }

            AppResult.Success(lista)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    suspend fun listarFuturasMedico(idMedico: String): AppResult<List<ConsultaDTO>> {
        return try {
            val medicoRef = medicosCollection.document(idMedico)
            val snapshot = consultasCollection
                .whereEqualTo("ref_medico", medicoRef)
                .get()
                .await()

            val agora = Timestamp.now()

            val lista = snapshot.documents.mapNotNull { doc ->
                val dataHora = doc.get("data_hora") as? Timestamp
                val status = doc.getString("status") ?: STATUS_AGENDADA
                val refMedico = doc.get("ref_medico") as? DocumentReference
                val refPaciente = doc.get("ref_paciente") as? DocumentReference

                if (dataHora != null && dataHora > agora && status != STATUS_CANCELADA) {
                    mapToConsultaDTO(
                        id = doc.id,
                        dataHora = dataHora,
                        refMedico = refMedico,
                        refPaciente = refPaciente,
                        status = status
                    )
                } else {
                    null
                }
            }.sortedBy { it.dataHoraConsulta }

            AppResult.Success(lista)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    suspend fun listarFuturas(idPaciente: String): AppResult<List<ConsultaDTO>> {
        return try {
            val pacienteRef = pacientesCollection.document(idPaciente)
            val snapshot = consultasCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .get()
                .await()

            val agora = Timestamp.now()

            val lista = snapshot.documents.mapNotNull { doc ->
                val dataHora = doc.get("data_hora") as? Timestamp
                val status = doc.getString("status") ?: STATUS_AGENDADA
                val refMedico = doc.get("ref_medico") as? DocumentReference
                val refPaciente = doc.get("ref_paciente") as? DocumentReference

                if (dataHora != null && dataHora > agora && status != STATUS_CANCELADA) {
                    mapToConsultaDTO(
                        id = doc.id,
                        dataHora = dataHora,
                        refMedico = refMedico,
                        refPaciente = refPaciente,
                        status = status
                    )
                } else {
                    null
                }
            }.sortedBy { it.dataHoraConsulta }

            AppResult.Success(lista)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    suspend fun listarPassadas(idPaciente: String): AppResult<List<ConsultaDTO>> {
        return try {
            val pacienteRef = pacientesCollection.document(idPaciente)
            val snapshot = consultasCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .get()
                .await()

            val agora = Timestamp.now()

            val lista = snapshot.documents.mapNotNull { doc ->
                val dataHora = doc.get("data_hora") as? Timestamp
                val status = doc.getString("status") ?: STATUS_AGENDADA
                val refMedico = doc.get("ref_medico") as? DocumentReference
                val refPaciente = doc.get("ref_paciente") as? DocumentReference

                if (dataHora != null && dataHora <= agora) {
                    mapToConsultaDTO(
                        id = doc.id,
                        dataHora = dataHora,
                        refMedico = refMedico,
                        refPaciente = refPaciente,
                        status = status
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.dataHoraConsulta }

            AppResult.Success(lista)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    // =========================================================
    // CADASTRAR / CANCELAR
    // =========================================================

    suspend fun cadastrar(body: CreateConsultaRequest): AppResult<Int> {
        return try {
            val medicoRef = medicosCollection.document(body.idMedico)
            val pacienteRef = pacientesCollection.document(body.idPaciente)
            val dataHoraTs = parseIsoToTimestamp(body.dataHoraConsulta)
            val agora = Timestamp.now()
            val currentUid = auth.currentUser?.uid

            val conflitosMedicoSnap = consultasCollection
                .whereEqualTo("ref_medico", medicoRef)
                .whereEqualTo("data_hora", dataHoraTs)
                .whereEqualTo("status", STATUS_AGENDADA)
                .get()
                .await()

            if (!conflitosMedicoSnap.isEmpty) {
                return AppResult.Success(409)
            }

            val conflitosPacienteSnap = consultasCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .whereEqualTo("data_hora", dataHoraTs)
                .whereEqualTo("status", STATUS_AGENDADA)
                .get()
                .await()

            if (!conflitosPacienteSnap.isEmpty) {
                return AppResult.Success(409)
            }

            val consultaData = hashMapOf(
                "data_cadastro" to agora,
                "data_hora" to dataHoraTs,
                "ref_medico" to medicoRef,
                "ref_paciente" to pacienteRef,
                "status" to STATUS_AGENDADA,
                "usuario_ultima_atualizacao" to currentUid
            )

            val consultaRef = consultasCollection.add(consultaData).await()

            medicoRef.update(
                "consultas",
                FieldValue.arrayUnion(consultaRef)
            ).await()

            pacienteRef.update(
                "consultas",
                FieldValue.arrayUnion(consultaRef)
            ).await()

            AppResult.Success(201)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    suspend fun cancelar(body: CancelarConsultaDTO): AppResult<Int> {
        return try {
            val idConsulta = body.idConsulta
                ?: return AppResult.Error(IllegalArgumentException("idConsulta é obrigatório para cancelar"))

            val consultaRef = consultasCollection.document(idConsulta)
            val snap = consultaRef.get().await()

            if (!snap.exists()) {
                return AppResult.Success(404)
            }

            val statusAtual = snap.getString("status") ?: STATUS_AGENDADA
            if (statusAtual == STATUS_CANCELADA) {
                return AppResult.Success(409)
            }

            val currentUid = auth.currentUser?.uid

            val updates = mapOf(
                "status" to STATUS_CANCELADA,
                "usuario_ultima_atualizacao" to currentUid,
                "justificativa_cancelamento" to (body.justificativa ?: "")
            )

            consultaRef.update(updates).await()

            AppResult.Success(200)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    // =========================================================
    // CONSULTAS DE HOJE
    // =========================================================

    /** 🔹 Retorna consultas AGENDADAS do paciente apenas para o dia de HOJE */
    suspend fun listarDeHojePorPaciente(idPaciente: String): AppResult<List<ConsultaDTO>> {
        return try {
            val pacienteRef = pacientesCollection.document(idPaciente)

            // 🔹 Busca TODAS as consultas do paciente (SIMPLÃO, como as outras)
            val snapshot = consultasCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .get()
                .await()

            // Definindo o intervalo de hoje [00:00, amanhã 00:00)
            val calInicio = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calFim = (calInicio.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            val inicioDia = calInicio.time
            val fimDia = calFim.time

            val lista = snapshot.documents.mapNotNull { doc ->
                val dataHora = doc.get("data_hora") as? Timestamp
                val status = doc.getString("status") ?: STATUS_AGENDADA
                val refMedico = doc.get("ref_medico") as? DocumentReference
                val refPaciente = doc.get("ref_paciente") as? DocumentReference

                val data = dataHora?.toDate() ?: return@mapNotNull null

                // 🔍 Filtro EM MEMÓRIA: só AGENDADA e entre início e fim do dia
                if (status != STATUS_AGENDADA) return@mapNotNull null
                if (data.before(inicioDia) || !data.before(fimDia)) return@mapNotNull null

                mapToConsultaDTO(
                    id = doc.id,
                    dataHora = dataHora,
                    refMedico = refMedico,
                    refPaciente = refPaciente,
                    status = status
                )
            }.sortedBy { it.dataHoraConsulta }

            AppResult.Success(lista)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
    // ConsultasFirebaseRepository.kt
    suspend fun listarDeHojeParaPacienteLogado(): AppResult<List<ConsultaDTO>> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return AppResult.Error(IllegalStateException("Usuário não autenticado"))

            val userSnap = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            // Se não tiver documento do usuário, apenas retorna lista vazia
            if (!userSnap.exists()) {
                // Log opcional
                android.util.Log.w(
                    "ConsultasRepo",
                    "Documento users/$uid não encontrado. Retornando lista vazia."
                )
                return AppResult.Success(emptyList())
            }

            val rawPacienteRef = userSnap.get("paciente_ref")

            // Aceita tanto DocumentReference quanto String (id simples)
            val idPaciente = when (rawPacienteRef) {
                is DocumentReference -> rawPacienteRef.id
                is String -> rawPacienteRef
                else -> null
            }

            if (idPaciente == null) {
                android.util.Log.w(
                    "ConsultasRepo",
                    "Campo 'paciente_ref' ausente ou inválido em users/$uid. Retornando lista vazia."
                )
                return AppResult.Success(emptyList())
            }

            listarDeHojePorPaciente(idPaciente)

        } catch (e: Exception) {
            android.util.Log.e("ConsultasRepo", "Erro em listarDeHojeParaPacienteLogado", e)
            AppResult.Error(e)
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private fun mapToConsultaDTO(
        id: String,
        dataHora: Timestamp?,
        refMedico: DocumentReference?,
        refPaciente: DocumentReference?,
        status: String?
    ): ConsultaDTO {
        val dataHoraIso = formatTimestamp(dataHora)
        val idMedico = refMedico?.id ?: ""
        val idPaciente = refPaciente?.id ?: ""

        return ConsultaDTO(
            id = id,
            dataHoraConsulta = dataHoraIso,
            idMedico = idMedico,
            idPaciente = idPaciente,
            status = status ?: STATUS_AGENDADA
        )
    }

    private fun mapToResumo(dto: ConsultaDTO): ConsultaResumoDTO {
        return ConsultaResumoDTO(
            id = dto.id,
            dataHoraConsulta = dto.dataHoraConsulta,
            idMedico = dto.idMedico,
            idPaciente = dto.idPaciente,
            status = dto.status
        )
    }

    private fun formatTimestamp(ts: Timestamp?): String {
        if (ts == null) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        return sdf.format(ts.toDate())
    }

    private fun parseIsoToTimestamp(iso: String): Timestamp {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
            val date: Date? = sdf.parse(iso)
            if (date != null) Timestamp(date) else Timestamp.now()
        } catch (e: Exception) {
            Timestamp.now()
        }
    }
}
