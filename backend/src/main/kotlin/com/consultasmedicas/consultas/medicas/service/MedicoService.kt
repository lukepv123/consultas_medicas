package com.consultasmedicas.consultas.medicas.service



import com.consultasmedicas.consultas.medicas.controller.dto.medicos.MedicoDTO
import com.consultasmedicas.consultas.medicas.model.Medico
import com.consultasmedicas.consultas.medicas.repository.MedicoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class MedicoService(
    private val repo: MedicoRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun criar(req: MedicoDTO, usuario: String): java.util.UUID {
        if (repo.existsByCrm(req.crm)) throw IllegalStateException("CRM já cadastrado")

        val entity = Medico(
            crm = req.crm.trim(),
            nome = req.nome.trim(),
            email = req.email.lowercase(),
            senhaHash = passwordEncoder.encode(req.senha),
            especialidade = req.especialidade,
            dataCadastro = OffsetDateTime.now(),
            dataUltimaAtualizacao = OffsetDateTime.now(),
            usuarioUltimaAtualizacao = usuario
        )
        return repo.save(entity).id!!
    }

    @Transactional(readOnly = true)
    fun listar(especialidade: String?, pageable: Pageable): Page<Medico> {
        if (especialidade.isNullOrBlank()) return repo.findAll(pageable)

        val enum = try {
            com.consultasmedicas.consultas.medicas.model.Especialidade.valueOf(especialidade.uppercase())
        } catch (_: Exception) {
            // contrato prevê 400 para parâmetros inválidos
            throw IllegalArgumentException("Especialidade inválida: $especialidade")
        }
        return repo.findByEspecialidade(enum, pageable)
    }
}