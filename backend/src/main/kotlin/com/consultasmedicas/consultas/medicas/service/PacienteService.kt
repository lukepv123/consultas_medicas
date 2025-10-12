package com.consultasmedicas.consultas.medicas.service
import com.consultasmedicas.consultas.medicas.controller.dto.pacientes.PacienteCreateDTO
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
    private val account: UserAccountService
) {
    @Transactional
    fun criar(req: PacienteCreateDTO, usuario: String): UUID {
        if (repo.existsByCpf(req.cpf)) throw IllegalStateException("CPF já cadastrado")

        val p = Paciente(
            cpf = req.cpf.trim(),
            nome = req.nome.trim(),
            dataCadastro = java.time.OffsetDateTime.now(),
            dataUltimaAtualizacao = java.time.OffsetDateTime.now(),
            usuarioUltimaAtualizacao = usuario
        )
        val saved = repo.save(p)

        // cria a conta (User + ROLE_PACIENTE) vinculada ao paciente
        account.createForPaciente(req.account.email, req.account.senha, saved)

        return saved.id!!
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): PacienteResponseDTO {
        val p = repo.findById(id).orElseThrow { jakarta.persistence.EntityNotFoundException("Paciente não encontrado") }
        // buscar e-mail a partir de users (se quiser mostrar)
        val email = p.id?.let { /* consulta leve ou projeção; pode criar um repo custom */ null }
        return PacienteResponseDTO(p.id!!, p.cpf, p.nome, email)
    }



    fun buscarIdPorCpf(cpfInput: String): UUID {
        val digits = cpfInput.filter { it.isDigit() }
        require(digits.length == 11) { "CPF deve conter 11 dígitos." }

        return repo.findIdByCpfDigits(digits)
            .orElseThrow { NoSuchElementException("Paciente não encontrado para o CPF informado.") }
    }


}
