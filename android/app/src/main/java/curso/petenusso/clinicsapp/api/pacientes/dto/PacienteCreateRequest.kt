package curso.petenusso.clinicsapp.api.pacientes.dto

import curso.petenusso.clinicsapp.api.dto.AccountDTO

data class PacienteCreateRequest(
    val cpf: String,
    val nome: String,
    val account: AccountDTO
)