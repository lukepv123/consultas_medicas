package com.consultasmedicas.consultas.medicas.repository

import com.consultasmedicas.consultas.medicas.model.Consulta
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.*

interface ConsultaRepository : JpaRepository<Consulta, UUID> {
    fun existsByIdMedicoAndDataHoraConsulta(idMedico: UUID, dataHoraConsulta: OffsetDateTime): Boolean
    fun existsByIdPacienteAndDataHoraConsulta(idPaciente: UUID, dataHoraConsulta: OffsetDateTime): Boolean

    fun findByIdPacienteAndDataHoraConsultaBeforeOrderByDataHoraConsultaDesc(
        idPaciente: UUID, limite: OffsetDateTime, pageable: Pageable
    ): Page<Consulta>

    fun findByIdPacienteAndDataHoraConsultaAfterOrderByDataHoraConsultaAsc(
        idPaciente: UUID, limite: OffsetDateTime, pageable: Pageable
    ): Page<Consulta>

    fun findByIdMedicoAndDataHoraConsultaAfterOrderByDataHoraConsultaAsc(
        idMedico: UUID, limite: OffsetDateTime, pageable: Pageable
    ): Page<Consulta>

    fun findByIdPacienteAndDataHoraConsulta(
        idPaciente: UUID,
        dataHoraConsulta: OffsetDateTime
    ): Optional<Consulta>

//-------------------------------------------------------------------
    // ➕ novos para checar apenas AGENDADAS
    fun existsByIdMedicoAndDataHoraConsultaAndStatus(
        idMedico: UUID,
        dataHoraConsulta: OffsetDateTime,
        status: String
    ): Boolean

    fun existsByIdPacienteAndDataHoraConsultaAndStatus(
        idPaciente: UUID,
        dataHoraConsulta: OffsetDateTime,
        status: String
    ): Boolean
}
