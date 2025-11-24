package curso.petenusso.clinicsapp.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteCreateRequest
import curso.petenusso.clinicsapp.api.pacientes.dto.PacienteDTO
import curso.petenusso.clinicsapp.core.AppResult
import kotlinx.coroutines.tasks.await

/**
 * Versão Firebase do PacienteRepository.
 *
 * Mantém os MESMOS métodos públicos:
 *  - cadastrar(body: PacienteCreateRequest): AppResult<Int>
 *  - buscarPaciente(id: String): AppResult<PacienteDTO>
 *  - buscarIdPorCpf(cpf: String): AppResult<String>
 *
 * Diferença: em vez de chamar a API via Retrofit, usa:
 *  - FirebaseAuth para criar usuário (email/senha)
 *  - Firestore para salvar em:
 *      - /pacientes/{pacienteId}
 *      - /users/{uid} com paciente_ref
 */
class PacienteFirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val pacientesCollection get() = firestore.collection("pacientes")
    private val usersCollection get() = firestore.collection("users")

    /**
     * 🔹 Cadastra novo paciente.
     *
     * Compatível com:
     *   suspend fun cadastrar(body: PacienteCreateRequest): AppResult<Int>
     *
     * Antes: retornava response.code() da API.
     * Agora: em caso de sucesso, retornamos 201 (Created).
     */
    suspend fun cadastrar(body: PacienteCreateRequest): AppResult<Int> {
        return try {
            // 1) Cria usuário no Firebase Auth (paciente)
            val cred = auth.createUserWithEmailAndPassword(
                body.account.email,
                body.account.senha
            ).await()

            val uid = cred.user?.uid
                ?: throw IllegalStateException("Falha ao criar usuário de paciente no FirebaseAuth")

            // 2) Cria documento em /pacientes
            val pacienteData = hashMapOf(
                "cpf" to body.cpf,
                "nome" to body.nome,
                "email" to body.account.email,
                "authUid" to uid,
                "data_cadastro" to Timestamp.now(),
                "usuario_ultima_atualizacao" to uid
            )

            val pacienteDocRef = pacientesCollection.add(pacienteData).await()

            // 3) Cria documento em /users/{uid} com referência para /pacientes/{pacienteId}
            val userData = hashMapOf(
                "medico_ref" to null,              // paciente não é médico
                "paciente_ref" to pacienteDocRef,  // reference para /pacientes/{pacienteId}
                "role" to "PACIENTE",              // papel do usuário
                "username" to body.account.email   // ou body.nome, se preferir
            )

            usersCollection.document(uid).set(userData).await()

            // 4) Retorna "status code" compatível (como a API antiga)
            AppResult.Success(201) // Created
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /**
     * 🔹 Busca paciente completo pelo ID (compatível com buscarPaciente).
     *
     * Antes: chamava API autenticada e retornava PacienteDTO.
     * Agora: lê /pacientes/{id} no Firestore.
     */
    suspend fun buscarPaciente(id: String): AppResult<PacienteDTO> {
        return try {
            val snapshot = pacientesCollection.document(id).get().await()

            if (!snapshot.exists()) {
                AppResult.Error(Throwable("Paciente não encontrado"))
            } else {
                val data = snapshot.data ?: emptyMap<String, Any?>()

                val cpf = data["cpf"] as? String ?: ""
                val nome = data["nome"] as? String ?: ""
                val email = data["email"] as? String

                val dto = PacienteDTO(
                    id = snapshot.id,
                    cpf = cpf,
                    nome = nome,
                    email = email
                )

                AppResult.Success(dto)
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    /**
     * 🔹 Busca apenas o ID do paciente pelo CPF (compatível com buscarIdPorCpf).
     *
     * Antes: API GET /pacientes/cpf/{cpf}/id retornava IdResponse{id}.
     * Agora: consulta Firestore em /pacientes onde cpf == {cpf} e pega o primeiro doc.
     */
    suspend fun buscarIdPorCpf(cpf: String): AppResult<String> {
        return try {
            val querySnapshot = pacientesCollection
                .whereEqualTo("cpf", cpf)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                AppResult.Error(Throwable("Paciente não encontrado"))
            } else {
                val doc = querySnapshot.documents.first()
                AppResult.Success(doc.id)
            }
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
