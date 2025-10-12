package curso.petenusso.clinicsapp.api.pacientes.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IdResponse(
    val id: String // UUID como String
)