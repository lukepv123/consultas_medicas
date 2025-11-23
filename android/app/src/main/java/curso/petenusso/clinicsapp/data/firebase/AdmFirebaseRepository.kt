package curso.petenusso.clinicsapp.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import curso.petenusso.clinicsapp.api.adm.dto.SetupStatusResponse
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.model.session.*
import kotlinx.coroutines.tasks.await

class AdmFirebaseRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Nome da collection de usuários no Firestore
    private val usersCollection = "users"
    private val medicosCollection = "medicos"
    private val pacientesCollection = "pacientes"

    /**
     * 🔹 Verifica status inicial do sistema usando Firestore
     * Equivalente ao GET /setup/status
     *
     * Regra:
     *  - Se existir pelo menos 1 documento em users com role == "ADMIN"
     *    então hasAdmin = true, senão false.
     */
    suspend fun status(): AppResult<SetupStatusResponse> = try {
        val snapshot = firestore
            .collection(usersCollection)
            .whereEqualTo("role", "ADMIN")
            .limit(1)
            .get()
            .await()

        val hasAdmin = !snapshot.isEmpty

        AppResult.Success(
            SetupStatusResponse(
                hasAdmin = hasAdmin
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        AppResult.Error(e)
    }

    /**
     * 🔹 Cria o administrador inicial no sistema
     * Equivalente ao POST /setup/admin
     *
     * - Cria usuário no FirebaseAuth (email/senha)
     * - Cria documento em users/{uid} com:
     *      role        = "ADMIN"
     *      username    = email (mesmo valor)
     *      email       = email
     *      medico_ref  = null
     *      paciente_ref= null
     *
     * Retorna o id (uid) do novo usuário admin.
     */
    suspend fun createAdmin(email: String, senha: String): AppResult<String> = try {
        // Cria usuário no Auth
        val result = auth.createUserWithEmailAndPassword(email, senha).await()
        val uid = result.user?.uid
            ?: throw IllegalStateException("Usuário criado sem UID no FirebaseAuth.")

        // Monta dados do documento em "users/{uid}"
        val userData = hashMapOf(
            "role" to "ADMIN",
            // username = email, conforme definido por você
            "username" to email,
            "email" to email,
            // Campo reference vazio -> simplesmente deixamos null
            "medico_ref" to null,
            "paciente_ref" to null
        )

        firestore
            .collection(usersCollection)
            .document(uid)
            .set(userData)
            .await()

        AppResult.Success(uid)
    } catch (e: Exception) {
        e.printStackTrace()
        AppResult.Error(e)
    }

    /**
     * 🔹 Realiza login e retorna sessão conforme o papel do usuário
     * Equivalente ao GET /auth/session (via Basic Auth) da sua API.
     *
     * Aqui:
     *  - emailOrUser é tratado como email (login por email + senha, como você definiu).
     *  - Fazemos signInWithEmailAndPassword no FirebaseAuth.
     *  - Buscamos users/{uid} para descobrir o role e refs de medico/paciente.
     *  - Montamos UserSession (AdmSession, MedicoSession, PacienteSession)
     *    com os mesmos campos que o AdminRepository atual.
     */
    suspend fun login(emailOrUser: String, password: String): AppResult<UserSession> = try {
        val emailToUse = emailOrUser // aqui consideramos sempre email

        // 1) Login no Firebase Auth
        val authResult = auth.signInWithEmailAndPassword(emailToUse, password).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("Login efetuado mas FirebaseUser é nulo.")

        val uid = firebaseUser.uid

        // 2) Buscar documento em users/{uid}
        val userDoc = firestore
            .collection(usersCollection)
            .document(uid)
            .get()
            .await()

        if (!userDoc.exists()) {
            throw IllegalStateException("Documento de usuário não encontrado em $usersCollection/$uid")
        }

        val role = userDoc.getString("role") ?: ""
        val roles = if (role.isNotBlank()) listOf(role) else emptyList<String>()

        Log.d("AdmFirebaseRepository", "Login role: $role, uid: $uid")

        val session: UserSession = when (role.uppercase()) {
            "ADMIN" -> {
                // Sessão de admin – mantém a mesma assinatura
                AdmSession(
                    emailOrUser = emailOrUser,
                    authPassword = password,
                    displayName = "Administrador",
                    userId = uid,
                    roles = roles
                )
            }

            "MEDICO" -> {
                val medicoRef = userDoc.getDocumentReference("medico_ref")
                val medicoData = loadMedicoData(medicoRef)

                MedicoSession(
                    emailOrUser = emailOrUser,
                    authPassword = password,
                    displayName = medicoData.nome ?: "Médico",
                    userId = uid,
                    roles = roles,
                    medicoId = medicoData.id,
                    crm = medicoData.crm,
                    nome = medicoData.nome,
                    especialidade = medicoData.especialidade
                )
            }

            "PACIENTE" -> {
                val pacienteRef = userDoc.getDocumentReference("paciente_ref")
                val pacienteData = loadPacienteData(pacienteRef)

                PacienteSession(
                    emailOrUser = emailOrUser,
                    authPassword = password,
                    displayName = pacienteData.nome ?: "Paciente",
                    userId = uid,
                    roles = roles,
                    pacienteId = pacienteData.id,
                    nome = pacienteData.nome,
                    cpf = pacienteData.cpf
                )
            }

            else -> error("Usuário sem role conhecida ou role vazia.")
        }

        AppResult.Success(session)
    } catch (e: Exception) {
        e.printStackTrace()
        AppResult.Error(e)
    }

    // =======================================================================
    // Helpers para carregar médico/paciente a partir das references
    // =======================================================================

    private data class MedicoData(
        val id: String? = null,
        val nome: String? = null,
        val crm: String? = null,
        val especialidade: String? = null
    )

    private data class PacienteData(
        val id: String? = null,
        val nome: String? = null,
        val cpf: String? = null
    )

    /**
     * Carrega dados do médico a partir de uma DocumentReference salva em users.medico_ref
     */
    private suspend fun loadMedicoData(medicoRef: DocumentReference?): MedicoData {
        if (medicoRef == null) return MedicoData()

        return try {
            val snap = medicoRef.get().await()
            if (!snap.exists()) return MedicoData()

            MedicoData(
                id = snap.id,
                nome = snap.getString("nome"),
                crm = snap.getString("crm"),
                especialidade = snap.getString("especialidade")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MedicoData()
        }
    }

    /**
     * Carrega dados do paciente a partir de uma DocumentReference salva em users.paciente_ref
     */
    private suspend fun loadPacienteData(pacienteRef: DocumentReference?): PacienteData {
        if (pacienteRef == null) return PacienteData()

        return try {
            val snap = pacienteRef.get().await()
            if (!snap.exists()) return PacienteData()

            PacienteData(
                id = snap.id,
                nome = snap.getString("nome"),
                cpf = snap.getString("cpf")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PacienteData()
        }
    }
}
