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
import java.util.Date
import java.util.Locale

/**
 * Versão Firebase do ConsultaRepository.
 *
 * Mantém MESMOS métodos públicos:
 *
 *  - suspend fun listarFuturasPorPaciente(idPaciente: String): AppResult<List<ConsultaResumoDTO>>
 *  - suspend fun listarFuturasMedico(idMedico: String): AppResult<List<ConsultaDTO>>
 *  - suspend fun listarFuturas(idPaciente: String): AppResult<List<ConsultaDTO>>
 *  - suspend fun listarPassadas(idPaciente: String): AppResult<List<ConsultaDTO>>
 *  - suspend fun cadastrar(body: CreateConsultaRequest): AppResult<Int>
 *  - suspend fun cancelar(body: CancelarConsultaDTO): AppResult<Int>
 *
 * Banco de dados (Firestore):
 *
 *  consultas [
 *      {consultaId} {
 *          data_cadastro: Timestamp
 *          data_hora: Timestamp
 *          ref_medico: /medicos/{medicoId} (DocumentReference)
 *          ref_paciente: /pacientes/{pacienteId} (DocumentReference)
 *          status: String
 *          usuario_ultima_atualizacao: String? (uid do usuário)
 *      }
 *  ]
 *
 *  medicos/{medicoId}.consultas  -> array<DocumentReference> de /consultas/{consultaId}
 *  pacientes/{pacienteId}.consultas -> array<DocumentReference> de /consultas/{consultaId}
 */
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

    /** 🔹 Retorna consultas futuras do paciente (equivalente ao PageEnvelope<ConsultaResumoDTO>.list()) */
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

    /** 🔹 Retorna consultas futuras de um médico (equivalente a PageEnvelope<ConsultaDTO>.list()) */
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

    /** 🔹 Retorna lista de futuras (equivalente a ConsultaListResponse.data) */
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

    /** 🔹 Retorna lista de passadas (equivalente a ConsultaListResponse.data) */
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
    // CADASTRAR CONSULTA
    // =========================================================

    /**
     * 🔹 Cadastra nova consulta (CreateConsultaRequest → AppResult<Int> com código estilo HTTP)
     *
     * - 201 → criada com sucesso
     * - 409 → já existe consulta para o mesmo paciente na mesma data/hora (conflito)
     */
    suspend fun cadastrar(body: CreateConsultaRequest): AppResult<Int> {
        return try {
            val medicoRef = medicosCollection.document(body.idMedico)
            val pacienteRef = pacientesCollection.document(body.idPaciente)
            val dataHoraTs = parseIsoToTimestamp(body.dataHoraConsulta)
            val agora = Timestamp.now()
            val currentUid = auth.currentUser?.uid

            // Verificar se já existe consulta nesse mesmo horário para o MESMO paciente
            val conflitosSnap = consultasCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .whereEqualTo("data_hora", dataHoraTs)
                .get()
                .await()

            if (!conflitosSnap.isEmpty) {
                // Simula HTTP 409 - CONFLICT
                return AppResult.Success(409)
            }

            // Monta dados da consulta
            val consultaData = hashMapOf(
                "data_cadastro" to agora,
                "data_hora" to dataHoraTs,
                "ref_medico" to medicoRef,
                "ref_paciente" to pacienteRef,
                "status" to STATUS_AGENDADA,
                "usuario_ultima_atualizacao" to currentUid
            )

            // 1) Cria doc em /consultas
            val consultaRef = consultasCollection.add(consultaData).await()

            // 2) Adiciona referência em /medicos/{idMedico}.consultas
            medicoRef.update(
                "consultas",
                FieldValue.arrayUnion(consultaRef)
            ).await()

            // 3) Adiciona referência em /pacientes/{idPaciente}.consultas
            pacienteRef.update(
                "consultas",
                FieldValue.arrayUnion(consultaRef)
            ).await()

            // Sucesso – simula HTTP 201 CREATED
            AppResult.Success(201)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    // =========================================================
    // CANCELAR CONSULTA
    // =========================================================

    /**
     * 🔹 Cancela uma consulta existente.
     *
     * Mantém a assinatura:
     *  suspend fun cancelar(body: CancelarConsultaDTO): AppResult<Int>
     *
     * Regras aqui:
     *  - Se idConsulta for nulo → Error
     *  - Se não encontrar a consulta → 404
     *  - Se já estiver CANCELADA → 409
     *  - Se cancelar com sucesso → 200
     *
     * (cpfPaciente / dataHoraConsulta são ignorados na busca; usamos idConsulta)
     */
    suspend fun cancelar(body: CancelarConsultaDTO): AppResult<Int> {
        return try {
            val idConsulta = body.idConsulta
                ?: return AppResult.Error(IllegalArgumentException("idConsulta é obrigatório para cancelar"))

            val consultaRef = consultasCollection.document(idConsulta)
            val snap = consultaRef.get().await()

            if (!snap.exists()) {
                // Simula HTTP 404 - NOT FOUND
                return AppResult.Success(404)
            }

            val statusAtual = snap.getString("status") ?: STATUS_AGENDADA
            if (statusAtual == STATUS_CANCELADA) {
                // Já está cancelada → simula 409 - CONFLICT
                return AppResult.Success(409)
            }

            val currentUid = auth.currentUser?.uid

            val updates = mapOf(
                "status" to STATUS_CANCELADA,
                "usuario_ultima_atualizacao" to currentUid,
                // opcional: salvar justificativa
                "justificativa_cancelamento" to (body.justificativa ?: "")
            )

            consultaRef.update(updates).await()

            // Sucesso – simula HTTP 200 OK
            AppResult.Success(200)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    // =========================================================
    // HELPERS / MAPEAMENTO DTOs
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

    /** Formata Timestamp em ISO-8601 aproximado (string) para o DTO */
    private fun formatTimestamp(ts: Timestamp?): String {
        if (ts == null) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        return sdf.format(ts.toDate())
    }

    /** Converte string ISO-8601 em Timestamp (fallback: now) */
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
