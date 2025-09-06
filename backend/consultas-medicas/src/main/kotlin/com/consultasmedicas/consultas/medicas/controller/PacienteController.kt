package com.consultasmedicas.consultas.medicas.controller
import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteDTO
import com.consultasmedicas.consultas.medicas.service.PacienteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/pacientes")
class PacienteController(
    private val service: PacienteService
) {

    // ➕ Cadastrar Paciente — 201 / 422 / 409  (401/403 via Security)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody body: PacienteDTO,
        @AuthenticationPrincipal user: UserDetails
    ): Map<String, Any> {
        val allowed = user.authorities.any { it.authority in listOf("ROLE_ADMIN", "ROLE_OPERADOR") }
        if (!allowed) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão")

        val id = try {
            service.criar(body, user.username)
        } catch (ex: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, ex.message ?: "Conflito")
        }
        return mapOf("id" to id)
    }

    // 🔎 Buscar Paciente por ID — 200 / 404  (401/403 via Security)
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: UUID) =
        try {
            service.buscarPorId(id)
        } catch (ex: jakarta.persistence.EntityNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        }
}