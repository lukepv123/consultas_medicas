package com.consultasmedicas.consultas.medicas.controller

import com.consultasmedicas.consultas.medicas.controller.common.PageEnvelope
import com.consultasmedicas.consultas.medicas.controller.common.toContractEnvelope
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoBasicResponseDTO
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoCreateDTO
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoResponseDTO
import com.consultasmedicas.consultas.medicas.service.MedicoService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/medicos")
class MedicoController(
    private val service: MedicoService
) {
    // ➕ Cadastrar Médico — ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody body: MedicoCreateDTO,
        @AuthenticationPrincipal user: UserDetails
    ): Map<String, Any> {
        val id = service.criar(body, user.username)
        return mapOf("id" to id)
    }

    // 📋 Listar Médicos — ADMIN/OPERADOR
    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @GetMapping
    fun listar(
        @RequestParam(required = false) especialidade: String?,
        pageable: Pageable
    ): PageEnvelope<MedicoResponseDTO> {
        if (pageable.pageNumber < 0 || pageable.pageSize < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        }
        return try { service.listarDTO(especialidade, pageable).toContractEnvelope { it } }
        catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
        }
    }



    // 🔹 Novo endpoint
    @GetMapping("/{id}/basico")
    fun buscarBasicoPorId(@PathVariable id: UUID): ResponseEntity<MedicoBasicResponseDTO> {
        val dto = service.buscarBasicoPorId(id)
        return ResponseEntity.ok(dto)
    }

}
