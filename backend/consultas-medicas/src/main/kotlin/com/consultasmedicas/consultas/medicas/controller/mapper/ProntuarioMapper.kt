package com.consultasmedicas.consultas.medicas.controller.mapper

import ProntuarioDTO
import com.consultasmedicas.consultas.medicas.controller.dto.prontuarios.ProntuarioResponseDTO
import com.consultasmedicas.consultas.medicas.model.Prontuario
import java.time.OffsetDateTime

object ProntuarioMapper {
    fun toEntity(req: ProntuarioDTO, usuario: String?): Prontuario =
        Prontuario(
            idPaciente = req.idPaciente,
            atendimento = req.atendimento,
            alergias = req.alergias,
            deficiencia = req.deficiencia,
            comorbidade = req.comorbidade,
            exames = req.exames,
            medicacao = req.medicacao,
            dataCadastro = OffsetDateTime.now(),
            dataUltimaAtualizacao = OffsetDateTime.now(),
            usuarioUltimaAtualizacao = usuario
        )

    fun toResponse(entity: Prontuario): ProntuarioResponseDTO =
        ProntuarioResponseDTO(
            id = entity.id!!,
            idPaciente = entity.idPaciente,
            atendimento = entity.atendimento,
            alergias = entity.alergias,
            deficiencia = entity.deficiencia,
            comorbidade = entity.comorbidade,
            exames = entity.exames,
            medicacao = entity.medicacao,
            dataCadastro = entity.dataCadastro,
            dataUltimaAtualizacao = entity.dataUltimaAtualizacao,
            usuarioUltimaAtualizacao = entity.usuarioUltimaAtualizacao
        )
}
