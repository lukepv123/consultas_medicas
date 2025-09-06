package com.consultasmedicas.consultas.medicas.controller



import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoDTO
import com.consultasmedicas.consultas.medicas.controller.common.PageEnvelope
import com.consultasmedicas.consultas.medicas.controller.common.toContractEnvelope
import com.consultasmedicas.consultas.medicas.controller.mapper.MedicoMapper
import com.consultasmedicas.consultas.medicas.service.MedicoService
import jakarta.validation.Valid
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoResponseDTO
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/medicos")
class MedicoController(
    private val service: MedicoService
) {

    // ➕ Cadastrar Médico — 201 / 422 / 409  (401/403 via Security)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody body: MedicoDTO,
        @AuthenticationPrincipal user: UserDetails
    ): Map<String, Any> {
        val isAdmin = user.authorities.any { it.authority == "ROLE_ADMIN" }
        if (!isAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Somente ADMIN pode cadastrar médicos")
        }
        val id = service.criar(body, user.username)
        return mapOf("id" to id)
    }

    // 📋 Listar Médicos — paginado + filtro, envelope do contrato
// 200 / 400 (param inválido)  (401/403 via Security)
    @GetMapping
    fun listar(
        @RequestParam(required = false) especialidade: String?,
        pageable: Pageable
    ): PageEnvelope<MedicoResponseDTO> {
        // 1) validação solicitada na doc
        if (pageable.pageNumber < 0 || pageable.pageSize < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page/per_page inválidos")
        }

        return try {
            // 2) usar mapper de response
            service.listar(especialidade, pageable)
                .toContractEnvelope(MedicoMapper::toDTO)
        } catch (ex: IllegalArgumentException) {
            // especialidade inválida, etc.
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
        }
    }
}