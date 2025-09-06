package com.consultasmedicas.consultas.medicas.service
import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteDTO
import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteResponseDTO
import com.consultasmedicas.consultas.medicas.model.Paciente
import com.consultasmedicas.consultas.medicas.repository.PacienteRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
class PacienteService(
    private val repo: PacienteRepository,
    private val encoder: PasswordEncoder
) {

    @Transactional
    fun criar(req: PacienteDTO, usuario: String): UUID {
        if (repo.existsByCpf(req.cpf)) throw IllegalStateException("CPF já cadastrado")

        val entity = Paciente(
            cpf = req.cpf.trim(),
            nome = req.nome.trim(),
            email = req.email.lowercase(),
            senhaHash = encoder.encode(req.senha),
            dataCadastro = OffsetDateTime.now(),
            dataUltimaAtualizacao = OffsetDateTime.now(),
            usuarioUltimaAtualizacao = usuario
        )
        return repo.save(entity).id!!
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): PacienteResponseDTO {
        val p = repo.findById(id).orElseThrow { EntityNotFoundException("Paciente não encontrado") }
        return PacienteResponseDTO(p.id!!, p.cpf, p.nome, p.email)
    }
}