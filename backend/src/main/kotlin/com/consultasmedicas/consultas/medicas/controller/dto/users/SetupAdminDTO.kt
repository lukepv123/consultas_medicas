package com.consultasmedicas.consultas.medicas.controller.dto.users

data class SetupAdminDTO( @field:jakarta.validation.constraints.Email val email: String,
                     @field:jakarta.validation.constraints.Size(min = 6, max = 80) val senha: String
)