package com.consultasmedicas.consultas.medicas.controller
import ProntuarioDTO
import com.consultasmedicas.consultas.medicas.controller.common.PageEnvelope
import com.consultasmedicas.consultas.medicas.controller.dto.prontuarios.ProntuarioResponseDTO
import com.consultasmedicas.consultas.medicas.service.ProntuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/prontuarios")
class ProntuarioController(
    private val service: ProntuarioService
) {

    // Médicos e Admin podem criar
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    fun criar(
        @Valid @RequestBody body: ProntuarioDTO,
        @AuthenticationPrincipal user: UserDetails
    ): ProntuarioResponseDTO {
        return service.criar(body, user.username)
    }

    // Médico pode listar histórico do paciente; Admin também
    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    fun listarPorPaciente(
        @PathVariable idPaciente: UUID,
        @RequestParam(name = "page", defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ): PageEnvelope<ProntuarioResponseDTO> {
        return service.listarPorPaciente(idPaciente, page, perPage)
    }
}