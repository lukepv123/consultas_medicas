package com.consultasmedicas.consultas.medicas.controller

import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteCreateDTO
import com.consultasmedicas.consultas.medicas.service.PacienteService
import jakarta.annotation.security.PermitAll
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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
    // ➕ Cadastrar Paciente — ABERTO (sem autenticação)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PermitAll
    fun criar(
        @Valid @RequestBody body: PacienteCreateDTO,
        @AuthenticationPrincipal user: UserDetails? // pode ser null quando anônimo
    ): Map<String, Any> {
        val actor = user?.username ?: "public"
        val id = try {
            service.criar(body, actor)
        } catch (ex: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, ex.message ?: "Conflito")
        }
        return mapOf("id" to id)
    }

    // 🔎 Buscar Paciente por ID — ADMIN/OPERADOR/MEDICO
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','MEDICO')")
    fun buscarPorId(@PathVariable id: UUID) =
        try { service.buscarPorId(id) }
        catch (ex: jakarta.persistence.EntityNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        }
}
