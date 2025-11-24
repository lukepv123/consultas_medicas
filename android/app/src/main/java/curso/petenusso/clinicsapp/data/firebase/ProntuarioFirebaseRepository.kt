package curso.petenusso.clinicsapp.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import curso.petenusso.clinicsapp.api.prontuarios.dto.CreateProntuarioRequest
import curso.petenusso.clinicsapp.api.prontuarios.dto.ProntuarioDTO
import curso.petenusso.clinicsapp.core.AppResult
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Versão Firebase do ProntuarioRepository.
 *
 * Mantém os MESMOS métodos públicos:
 *  - listarPorPaciente(idPaciente: String): AppResult<List<ProntuarioDTO>>
 *  - criarProntuario(body: CreateProntuarioRequest): AppResult<ProntuarioDTO>
 *
 * Diferença: em vez de chamar a API via Retrofit, usa Firestore:
 *  - /prontuarios/{prontuarioId}
 *  - /pacientes/{pacienteId} com array "prontuarios" de referências
 */
class ProntuarioFirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val prontuariosCollection get() = firestore.collection("prontuarios")
    private val pacientesCollection get() = firestore.collection("pacientes")

    /**
     * 🔹 Lista prontuários de um paciente.
     *
     * Compatível com:
     *  suspend fun listarPorPaciente(idPaciente: String): AppResult<List<ProntuarioDTO>>
     */
    suspend fun listarPorPaciente(idPaciente: String): AppResult<List<ProntuarioDTO>> {
        return try {
            val pacienteRef = pacientesCollection.document(idPaciente)

            val snapshot = prontuariosCollection
                .whereEqualTo("ref_paciente", pacienteRef)
                .get()
                .await()

            val lista = snapshot.documents.map { doc ->
                mapDocToProntuarioDTO(doc.id, doc.data, doc.get("ref_paciente") as? DocumentReference)
            }

            AppResult.Success(lista)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /**
     * 🔹 Cria um novo prontuário para um paciente.
     *
     * Compatível com:
     *  suspend fun criarProntuario(body: CreateProntuarioRequest): AppResult<ProntuarioDTO>
     */
    suspend fun criarProntuario(body: CreateProntuarioRequest): AppResult<ProntuarioDTO> {
        return try {
            val currentUid = auth.currentUser?.uid

            // Referência do paciente
            val pacienteRef = pacientesCollection.document(body.idPaciente)

            val agora = Timestamp.now()

            // Dados salvos em /prontuarios
            val prontuarioData = hashMapOf(
                "atendimento" to body.atendimento,
                "alergias" to body.alergias,
                "deficiencia" to body.deficiencia,
                "comorbidade" to body.comorbidade,
                "exames" to body.exames,
                "medicacao" to body.medicacao,
                "ref_paciente" to pacienteRef,
                "data_cadastro" to agora,
                // pode começar null e ser preenchido em updates futuros
                "data_ultima_atualizacao" to null,
                "usuario_ultima_atualizacao" to currentUid
            )

            // 1) Cria doc em /prontuarios
            val prontuarioRef = prontuariosCollection.add(prontuarioData).await()

            // 2) Atualiza /pacientes/{idPaciente} adicionando referência no array 'prontuarios'
            pacienteRef.update(
                "prontuarios",
                FieldValue.arrayUnion(prontuarioRef)
            ).await()

            // 3) Monta DTO de retorno compatível com o antigo ProntuarioDTO
            val dto = ProntuarioDTO(
                id = prontuarioRef.id,
                idPaciente = body.idPaciente,
                atendimento = body.atendimento,
                alergias = body.alergias,
                deficiencia = body.deficiencia,
                comorbidade = body.comorbidade,
                exames = body.exames,
                medicacao = body.medicacao,
                dataCadastro = formatTimestamp(agora),
                dataUltimaAtualizacao = null,
                usuarioUltimaAtualizacao = currentUid
            )

            AppResult.Success(dto)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    /**
     * Converte os dados do Firestore em um ProntuarioDTO, compatível com o DTO da API.
     */
    private fun mapDocToProntuarioDTO(
        docId: String,
        rawData: Map<String, Any?>?,
        refPaciente: DocumentReference?
    ): ProntuarioDTO {
        val data = rawData ?: emptyMap<String, Any?>()

        val atendimento = data["atendimento"] as? String ?: ""
        val alergias = data["alergias"] as? String
        val deficiencia = data["deficiencia"] as? String
        val comorbidade = data["comorbidade"] as? String
        val exames = data["exames"] as? String
        val medicacao = data["medicacao"] as? String

        val tsCadastro = data["data_cadastro"] as? Timestamp
        val tsUltima = data["data_ultima_atualizacao"] as? Timestamp
        val usuarioUltima = data["usuario_ultima_atualizacao"] as? String

        val idPaciente = refPaciente?.id ?: (data["idPaciente"] as? String ?: "")

        return ProntuarioDTO(
            id = docId,
            idPaciente = idPaciente,
            atendimento = atendimento,
            alergias = alergias,
            deficiencia = deficiencia,
            comorbidade = comorbidade,
            exames = exames,
            medicacao = medicacao,
            dataCadastro = formatTimestamp(tsCadastro),
            dataUltimaAtualizacao = tsUltima?.let { formatTimestamp(it) },
            usuarioUltimaAtualizacao = usuarioUltima
        )
    }

    /**
     * Formata Timestamp em String, para bater com o tipo do DTO da API.
     * (pode ajustar o padrão se quiser algo específico)
     */
    private fun formatTimestamp(ts: Timestamp?): String {
        if (ts == null) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        return sdf.format(ts.toDate())
    }
}
