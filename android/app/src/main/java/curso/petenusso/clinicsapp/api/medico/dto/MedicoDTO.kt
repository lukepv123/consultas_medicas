package curso.petenusso.clinicsapp.api.medico.dto

data class MedicoDTO(
    val id: String? = null,
    val crm: String,
    val nome: String,
    val especialidade: String,
    val email: String? = null
)