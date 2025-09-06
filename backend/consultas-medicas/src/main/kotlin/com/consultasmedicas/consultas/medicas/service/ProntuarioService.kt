package com.consultasmedicas.consultas.medicas.service

import ProntuarioDTO
import com.consultasmedicas.consultas.medicas.controller.common.PageEnvelope
import com.consultasmedicas.consultas.medicas.controller.dto.prontuarios.ProntuarioResponseDTO
import com.consultasmedicas.consultas.medicas.controller.mapper.ProntuarioMapper
import com.consultasmedicas.consultas.medicas.model.Prontuario
import com.consultasmedicas.consultas.medicas.repository.ProntuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.math.ceil

@Service
class ProntuarioService(
    private val prontuarioRepository: ProntuarioRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun criar(req: ProntuarioDTO, usuario: String?): ProntuarioResponseDTO {
        // Se quiser validar existência do paciente:
        // if (!pacienteRepository.existsById(req.idPaciente))
        //     throw NoSuchElementException("Paciente não encontrado")

        val entity: Prontuario = ProntuarioMapper.toEntity(req, usuario)
        val saved = prontuarioRepository.save(entity)

        log.info("prontuario_created id={} idPaciente={} user={}", saved.id, saved.idPaciente, usuario)
        return ProntuarioMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun listarPorPaciente(
        idPaciente: UUID,
        page: Int,
        perPage: Int
    ): PageEnvelope<ProntuarioResponseDTO> {
        require(page >= 1) { "page deve ser >= 1" }
        require(perPage >= 1) { "per_page deve ser >= 1" }

        val pageable = PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "dataCadastro"))
        val pageResult = prontuarioRepository.findByIdPaciente(idPaciente, pageable)

        val data: List<ProntuarioResponseDTO> = pageResult.content.map { ProntuarioMapper.toResponse(it) }
        val totalItems: Long = pageResult.totalElements
        val totalPages: Int = ceil(totalItems / perPage.toDouble()).toInt().coerceAtLeast(1)

        log.info(
            "prontuario_list_by_paciente idPaciente={} page={} perPage={} totalItems={}",
            idPaciente, page, perPage, totalItems
        )

        return PageEnvelope(
            current_page = page,
            total_pages = totalPages,
            total_items = totalItems,
            per_page = perPage,
            data = data
        )
    }
}
