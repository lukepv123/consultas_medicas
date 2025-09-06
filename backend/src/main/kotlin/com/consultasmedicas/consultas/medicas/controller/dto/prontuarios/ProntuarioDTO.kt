import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

// ---------- REQUESTS ----------
data class ProntuarioDTO(
    @field:NotNull val idPaciente: UUID,
    @field:NotBlank val atendimento: String,
    val alergias: String? = null,
    val deficiencia: String? = null,
    val comorbidade: String? = null,
    val exames: String? = null,
    val medicacao: String? = null
)

