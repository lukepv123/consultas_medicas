package com.consultasmedicas.consultas.medicas.controller.dto.users

import java.util.UUID

data class AdminSessionDTO(
    val type: String = "ADMIN",
    val userId: UUID,
    val email: String,
    val roles: List<String>
)