package curso.petenusso.clinicsapp.api.medico.dto

/**
 * Corpo de criação de médico exigido pelo servidor:
 * {
 *   "crm": "52562672721",
 *   "nome": "João da Silvasss",
 *   "especialidade": "PEDIATRIA",
 *   "account": { "email": "joao@medico.com", "senha": "Medico#2025" }
 * }
 */
data class CreateMedicoRequest(
    val crm: String,
    val nome: String,
    val especialidade: String,
    val account: AccountDTO
)