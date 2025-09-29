package com.consultasmedicas.consultas.medicas.controller



import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoEntityDTO
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoSessionDTO
import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteEntityDTO
import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteSessionDTO
import com.consultasmedicas.consultas.medicas.controller.dto.users.AdminSessionDTO
import com.consultasmedicas.consultas.medicas.repository.UserRepository
import com.consultasmedicas.consultas.medicas.repository.UserRoleRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
class AuthController(
    private val users: UserRepository,
    private val rolesRepo: UserRoleRepository
) {



    @GetMapping("/session")
    @jakarta.annotation.security.PermitAll
    fun session(@AuthenticationPrincipal principal: UserDetails): Any {
        val u = users.findByUsernameWithLinks(principal.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")

        val roles = rolesRepo.findByUserId(u.id!!).map { it.role }

        return when {
            "ADMIN" in roles -> {
                AdminSessionDTO(
                    userId = u.id!!,
                    email = u.username,
                    roles = roles
                )
            }
            "MEDICO" in roles -> {
                val m = u.medico ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Usuário com ROLE_MEDICO sem vínculo Medico"
                )
                MedicoSessionDTO(
                    userId = u.id!!,
                    email = u.username,
                    roles = roles,
                    medico = MedicoEntityDTO(
                        id = m.id!!,
                        crm = m.crm,
                        nome = m.nome,
                        especialidade = m.especialidade.name,
                        dataCadastro = m.dataCadastro,
                        dataUltimaAtualizacao = m.dataUltimaAtualizacao,
                        usuarioUltimaAtualizacao = m.usuarioUltimaAtualizacao
                    )
                )
            }
            "PACIENTE" in roles -> {
                val p = u.paciente ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Usuário com ROLE_PACIENTE sem vínculo Paciente"
                )
                PacienteSessionDTO(
                    userId = u.id!!,
                    email = u.username,
                    roles = roles,
                    paciente = PacienteEntityDTO(
                        id = p.id!!,
                        cpf = p.cpf,
                        nome = p.nome,
                        dataCadastro = p.dataCadastro,
                        dataUltimaAtualizacao = p.dataUltimaAtualizacao,
                        usuarioUltimaAtualizacao = p.usuarioUltimaAtualizacao
                    )
                )
            }
            else -> throw ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem role conhecida")
        }
    }
}