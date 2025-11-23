package curso.petenusso.clinicsapp.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import curso.petenusso.clinicsapp.api.medico.dto.CreateMedicoResponse
import curso.petenusso.clinicsapp.api.medico.dto.MedicoDTO
import curso.petenusso.clinicsapp.api.medico.dto.MedicoListResponse
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.runCatchingResult
import kotlinx.coroutines.tasks.await

/**
 * Versão Firebase do MedicoRepository.
 *
 * Mantém os MESMOS métodos públicos:
 *  - criarComAccount(...)
 *  - listar()
 *
 * Implementação:
 *  - Usa FirebaseAuth para criar usuário (email/senha)
 *  - Salva dados exclusivos do médico em /medicos
 *  - Salva dados de usuário em /users com medico_ref != null
 */
class MedicoFirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val medicosCollection get() = firestore.collection("medicos")
    private val usersCollection get() = firestore.collection("users")

    /**
     * Mesmo nome, parâmetros e tipo de retorno do MedicoRepository antigo.
     *
     * Retorna: AppResult<CreateMedicoResponse>
     */
    suspend fun criarComAccount(
        crm: String,
        nome: String,
        especialidadeEnumName: String,
        email: String,
        senha: String
    ): AppResult<CreateMedicoResponse> = runCatchingResult {

        // 1) Cria usuário no Firebase Auth
        val userCred = auth.createUserWithEmailAndPassword(email, senha).await()
        val uid = userCred.user?.uid
            ?: error("Falha ao obter UID do usuário criado no Firebase Auth")

        // 2) Salva os dados EXCLUSIVOS de médico no Firestore em /medicos
        val medicoData = hashMapOf(
            // modelo da coleção "medicos" que você definiu
            "crm" to crm,
            "nome" to nome,
            "especialidade" to especialidadeEnumName,
            "data_cadastro" to Timestamp.now(),
            "usuario_ultima_atualizacao" to email, // ou uid, ou outro usuário responsável
            "consultas" to emptyList<DocumentReference>() // lista vazia de refs no começo
        )

        // cria o documento do médico
        val medicoDocRef = medicosCollection.add(medicoData).await()
        val medicoId = medicoDocRef.id

        // 3) Cria o documento em /users/{uid}, ligando com medico_ref
        val userData = hashMapOf(
            "medico_ref" to medicoDocRef,
            "paciente_ref" to null,
            "role" to "MEDICO",
            "username" to email // aqui pode ser email, ou outro username se futuramente tiver
        )

        // usamos o próprio UID do Auth como ID do doc em /users
        usersCollection.document(uid).set(userData).await()

        // 4) DTO de resposta da API original tem só "id"
        CreateMedicoResponse(id = medicoId)
    }

    /**
     * Mesmo nome e retorno do MedicoRepository.listar().
     *
     * Retorna: AppResult<MedicoListResponse>
     * Lendo diretamente da coleção /medicos.
     */
    suspend fun listar(): AppResult<MedicoListResponse> = runCatchingResult {
        val snapshot = medicosCollection.get().await()
        mapFirestoreToMedicoListResponse(snapshot)
    }

    // =========================================================
    // MAPEAMENTOS Firestore -> DTOs
    // =========================================================

    /**
     * Converte o snapshot de /medicos em um MedicoListResponse.
     */
    private fun mapFirestoreToMedicoListResponse(
        snapshot: QuerySnapshot
    ): MedicoListResponse {
        val medicos: List<MedicoDTO> = snapshot.documents.map { doc ->
            val data = doc.data ?: emptyMap<String, Any?>()

            val id = doc.id
            val crm = data["crm"] as? String ?: ""
            val nome = data["nome"] as? String ?: ""
            val especialidade = data["especialidade"] as? String ?: ""
            // email é dado de usuário -> fica em /users; aqui deixamos null
            val email: String? = null

            MedicoDTO(
                id = id,
                crm = crm,
                nome = nome,
                especialidade = especialidade,
                email = email
            )
        }

        val totalItems = medicos.size
        return MedicoListResponse(
            current_page = 1,
            total_pages = 1,
            total_items = totalItems,
            per_page = totalItems,
            data = medicos
        )
    }
}
