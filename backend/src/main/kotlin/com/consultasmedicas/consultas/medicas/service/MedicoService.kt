package com.consultasmedicas.consultas.medicas.service



import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoBasicResponseDTO
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoCreateDTO
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoDTO
import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoResponseDTO
import com.consultasmedicas.consultas.medicas.controller.mapper.MedicoMapper
import com.consultasmedicas.consultas.medicas.model.Especialidade
import com.consultasmedicas.consultas.medicas.model.Medico
import com.consultasmedicas.consultas.medicas.repository.MedicoRepository
import com.consultasmedicas.consultas.medicas.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class MedicoService(
    private val repo: MedicoRepository,
    private val account: UserAccountService,
    private val users: UserRepository
) {
    @Transactional
    fun criar(req: MedicoCreateDTO, usuario: String): UUID {
        if (repo.existsByCrm(req.crm)) throw IllegalStateException("CRM já cadastrado")

        val medico = Medico(
            crm = req.crm.trim(),
            nome = req.nome.trim(),
            especialidade = req.especialidade,
            dataCadastro = java.time.OffsetDateTime.now(),
            dataUltimaAtualizacao = java.time.OffsetDateTime.now(),
            usuarioUltimaAtualizacao = usuario
        )
        val saved = repo.save(medico)

        // cria a conta (User + ROLE_MEDICO) vinculada ao médico
        account.createForMedico(req.account.email, req.account.senha, saved)

        return saved.id!!
    }

    @Transactional(readOnly = true)
    fun listar(especialidade: String?, pageable: org.springframework.data.domain.Pageable)
            : org.springframework.data.domain.Page<Medico> {
        if (especialidade.isNullOrBlank()) return repo.findAll(pageable)
        val enum = try {
            Especialidade.valueOf(especialidade.uppercase())
        } catch (_: Exception) {
            throw IllegalArgumentException("Especialidade inválida: $especialidade")
        }
        return repo.findByEspecialidade(enum, pageable)
    }


    @Transactional(readOnly = true)
    fun listarDTO(especialidade: String?, pageable: Pageable): Page<MedicoResponseDTO> {
        // busca a página de Medico como antes
        val page: Page<Medico> = if (especialidade.isNullOrBlank()) {
            repo.findAll(pageable)
        } else {
            val enum = try { Especialidade.valueOf(especialidade.uppercase()) }
            catch (_: Exception) { throw IllegalArgumentException("Especialidade inválida: $especialidade") }
            repo.findByEspecialidade(enum, pageable)
        }

        // batch: busca todos os users por medico_id, evita N+1
        val ids = page.content.mapNotNull { it.id }
        val usersByMedico = if (ids.isEmpty()) emptyMap<UUID, String>()
        else users.findByMedicoIdIn(ids)
            .groupBy { it.medico!!.id!! }
            .mapValues { (_, list) -> list.first().username } // assume 1 conta por médico

        return page.map { m -> MedicoMapper.toDTO(m, usersByMedico[m.id!!]) }
    }



    // 🆕 NOVO MÉTODO — Buscar dados básicos do médico por ID
    @Transactional(readOnly = true)
    fun buscarBasicoPorId(id: UUID): MedicoBasicResponseDTO {
        val medico = repo.findById(id)
            .orElseThrow { EntityNotFoundException("Médico não encontrado para o ID informado") }

        return MedicoBasicResponseDTO(
            id = medico.id.toString(),
            crm = medico.crm,
            nome = medico.nome,
            especialidade = medico.especialidade.name
        )
    }


}