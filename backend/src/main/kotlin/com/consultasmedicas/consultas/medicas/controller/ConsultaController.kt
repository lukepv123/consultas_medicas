package com.consultasmedicas.consultas.medicas.controller

import com.consultasmedicas.consultas.medicas.controller.common.toContractEnvelope
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.CancelarConsultaDTO
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.CancelarConsultaPorCpfDTO
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.ConsultaDTO
import com.consultasmedicas.consultas.medicas.repository.UserRepository
import com.consultasmedicas.consultas.medicas.service.ConsultaService
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/consultas")
class ConsultaController(
    private val service: ConsultaService,
    private val users: UserRepository // (opcional remover; não é mais usado aqui)
) {
    // ➕ Cadastrar Consulta — ADMIN/PACIENTE (mantido)
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody body: ConsultaDTO,
        @AuthenticationPrincipal user: UserDetails
    ): Map<String, Any> {
        val id = try {
            service.criar(body, user.username)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
        } catch (ex: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, ex.message)
        }
        return mapOf("id" to id)
    }

    // 📋 Passadas (Paciente) — ADMIN/PACIENTE (mantido, por ID)
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @GetMapping("/paciente/{idPaciente}/passadas")
    fun listarPassadasPaciente(
        @PathVariable idPaciente: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        if (page < 1 || perPage < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        service.listarPassadasPaciente(idPaciente, page, perPage).toContractEnvelope(service::toResponse)
    }

    // 📋 Futuras (Paciente) — ADMIN/PACIENTE (mantido, por ID)
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @GetMapping("/paciente/{idPaciente}/futuras")
    fun listarFuturasPaciente(
        @PathVariable idPaciente: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        if (page < 1 || perPage < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        service.listarFuturasPaciente(idPaciente, page, perPage).toContractEnvelope(service::toResponse)
    }

    // 📋 Futuras (Médico) — ADMIN/PACIENTE/MEDICO (mantido)
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE','MEDICO')")
    @GetMapping("/medico/{idMedico}/futuras")
    fun listarFuturasMedico(
        @PathVariable idMedico: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        if (page < 1 || perPage < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        service.listarFuturasMedico(idMedico, page, perPage).toContractEnvelope(service::toResponse)
    }

    // ❌ Cancelar Consulta — ADMIN (mantido)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cancelamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelar(
        @Valid @RequestBody body: CancelarConsultaDTO,
        @AuthenticationPrincipal user: UserDetails
    ) {
        try {
            service.cancelar(body, user.username)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
        } catch (ex: NoSuchElementException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        } catch (ex: EntityNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        }
    }

    // =========================
    //   NOVOS ENDPOINTS (CPF)
    // =========================

    // 📋 Passadas (Paciente) — por CPF (novo) — ADMIN/PACIENTE
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @GetMapping("/paciente/cpf/{cpf}/passadas")
    fun listarPassadasPacientePorCpf(
        @PathVariable cpf: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        validarPaginacao(page, perPage)
        service.listarPassadasPacientePorCpf(cpf, page, perPage).toContractEnvelope(service::toResponse)
    }

    // 📋 Futuras (Paciente) — por CPF (novo) — ADMIN/PACIENTE
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @GetMapping("/paciente/cpf/{cpf}/futuras")
    fun listarFuturasPacientePorCpf(
        @PathVariable cpf: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ) = run {
        validarPaginacao(page, perPage)
        service.listarFuturasPacientePorCpf(cpf, page, perPage).toContractEnvelope(service::toResponse)
    }

    // ❌ Cancelar Consulta — ADMIN (novo, por CPF + data/hora)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cancelamento/cpf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelarPorCpf(
        @Valid @RequestBody body: CancelarConsultaPorCpfDTO,
        @AuthenticationPrincipal user: UserDetails
    ) {
        try {
            service.cancelarPorCpfEData(body, user.username)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
        } catch (ex: NoSuchElementException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        } catch (ex: jakarta.persistence.EntityNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.message)
        }
    }

    // ---- helpers ----
    private fun validarPaginacao(page: Int, perPage: Int) {
        if (page < 1 || perPage < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        }
    }
}
