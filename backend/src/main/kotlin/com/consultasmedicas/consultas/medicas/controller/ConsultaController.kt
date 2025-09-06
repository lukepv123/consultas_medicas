package com.consultasmedicas.consultas.medicas.controller

import com.consultasmedicas.consultas.medicas.controller.common.toContractEnvelope
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.CancelarConsultaDTO
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.ConsultaDTO
import com.consultasmedicas.consultas.medicas.service.ConsultaService
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/consultas")
class ConsultaController(
    private val service: ConsultaService
) {
    // ➕ Cadastrar Consulta — 201 / 422 / 409 (401/403 via Security)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody body: ConsultaDTO,
        @AuthenticationPrincipal user: UserDetails
    ): Map<String, Any> {
        val allowed = user.authorities.any { it.authority in listOf("ROLE_ADMIN", "ROLE_OPERADOR") }
        if (!allowed) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão")

        val id = try {
            service.criar(body, user.username)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
        } catch (ex: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, ex.message)
        }
        return mapOf("id" to id)
    }

    // 📋 Passadas (Paciente) — 200 / 400
    @GetMapping("/paciente/{idPaciente}/passadas")
    fun listarPassadasPaciente(
        @PathVariable idPaciente: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        if (page < 1 || perPage < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        service.listarPassadasPaciente(idPaciente, page, perPage).toContractEnvelope(service::toResponse)
    }

    // 📋 Futuras (Paciente) — 200 / 400
    @GetMapping("/paciente/{idPaciente}/futuras")
    fun listarFuturasPaciente(
        @PathVariable idPaciente: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        if (page < 1 || perPage < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        service.listarFuturasPaciente(idPaciente, page, perPage).toContractEnvelope(service::toResponse)
    }

    // 📋 Futuras (Médico) — 200 / 400
    @GetMapping("/medico/{idMedico}/futuras")
    fun listarFuturasMedico(
        @PathVariable idMedico: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        if (page < 1 || perPage < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        service.listarFuturasMedico(idMedico, page, perPage).toContractEnvelope(service::toResponse)
    }


    // ❌ Cancelar Consulta — 204 / 422 / 404  (401/403 via Security)
    @PostMapping("/cancelamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelar(
        @Valid @RequestBody body: CancelarConsultaDTO,
        @AuthenticationPrincipal user: UserDetails
    ) {
        val isAdmin = user.authorities.any { it.authority == "ROLE_ADMIN" }
        if (!isAdmin) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Somente ADMIN pode cancelar consultas")

        try {
            service.cancelar(body, user.username) // <<<<< AQUI: passa o DTO
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
        } catch (ex: NoSuchElementException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        } catch (ex: EntityNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        }
    }


}