package com.consultasmedicas.consultas.medicas.service

import com.consultasmedicas.consultas.medicas.controller.dto.consultas.CancelarConsultaDTO
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.ConsultaDTO
import com.consultasmedicas.consultas.medicas.controller.dto.consultas.ConsultaResponseDTO
import com.consultasmedicas.consultas.medicas.model.Consulta
import com.consultasmedicas.consultas.medicas.repository.ConsultaRepository
import com.consultasmedicas.consultas.medicas.repository.PacienteRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

@Service
class ConsultaService(
    private val repo: ConsultaRepository,
    private val pacienteRepo: PacienteRepository   // <-- adicionado

) {
    private val zona = ZoneId.of("America/Sao_Paulo")
    private val inicio = LocalTime.of(8, 0)
    private val fim = LocalTime.of(18, 0)

    @Transactional
    fun criar(req: ConsultaDTO, usuario: String): UUID {
        val agora = OffsetDateTime.now()
        if (req.dataHoraConsulta.isBefore(agora)) {
            throw IllegalArgumentException("Data/hora no passado não é permitida") // 422
        }
        val local = req.dataHoraConsulta.atZoneSameInstant(zona).toLocalTime()
        if (local.isBefore(inicio) || local.isAfter(fim)) {
            throw IllegalArgumentException("Horário fora da janela permitida (08:00–18:00)") // 422
        }
        if (repo.existsByIdMedicoAndDataHoraConsulta(req.idMedico, req.dataHoraConsulta) ||
            repo.existsByIdPacienteAndDataHoraConsulta(req.idPaciente, req.dataHoraConsulta)
        ) {
            throw IllegalStateException("Agenda indisponível para o horário solicitado") // 409
        }

        val entity = Consulta(
            dataHoraConsulta = req.dataHoraConsulta,
            idMedico = req.idMedico,
            idPaciente = req.idPaciente,
            status = "AGENDADA",
            usuarioUltimaAtualizacao = usuario
        )
        return repo.save(entity).id!!
    }

    @Transactional(readOnly = true)
    fun listarPassadasPaciente(idPaciente: UUID, page: Int, perPage: Int): Page<Consulta> {
        val pageable: Pageable = PageRequest.of(page - 1, perPage)
        return repo.findByIdPacienteAndDataHoraConsultaBeforeOrderByDataHoraConsultaDesc(
            idPaciente, OffsetDateTime.now(), pageable
        )
    }

    @Transactional(readOnly = true)
    fun listarFuturasPaciente(idPaciente: UUID, page: Int, perPage: Int): Page<Consulta> {
        val pageable: Pageable = PageRequest.of(page - 1, perPage)
        return repo.findByIdPacienteAndDataHoraConsultaAfterOrderByDataHoraConsultaAsc(
            idPaciente, OffsetDateTime.now(), pageable
        )
    }

    @Transactional(readOnly = true)
    fun listarFuturasMedico(idMedico: UUID, page: Int, perPage: Int): Page<Consulta> {
        val pageable: Pageable = PageRequest.of(page - 1, perPage)
        return repo.findByIdMedicoAndDataHoraConsultaAfterOrderByDataHoraConsultaAsc(
            idMedico, OffsetDateTime.now(), pageable
        )
    }

    fun toResponse(c: Consulta) = ConsultaResponseDTO(
        id = c.id!!,
        dataHoraConsulta = c.dataHoraConsulta,
        idMedico = c.idMedico,
        idPaciente = c.idPaciente,
        status = c.status
    )

    @Transactional
    fun cancelar(dto: CancelarConsultaDTO, usuario: String) {
        val paciente = pacienteRepo.findByCpf(dto.cpfPaciente.trim())
            .orElseThrow { EntityNotFoundException("Paciente não encontrado") }

        val consulta = repo.findByIdPacienteAndDataHoraConsulta(paciente.id!!, dto.dataHoraConsulta)
            .orElseThrow { EntityNotFoundException("Consulta nao encontrada") }

        if (consulta.status != "AGENDADA") throw IllegalArgumentException("Consulta não pode ser cancelada")

        consulta.status = "CANCELADA"
        consulta.usuarioUltimaAtualizacao = usuario
        consulta.dataUltimaAtualizacao = OffsetDateTime.now()
        repo.save(consulta)
    }
}