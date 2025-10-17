package com.consultasmedicas.consultas.medicas.model


import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "consultas")
class Consulta(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "data_hora", nullable = false)
    var dataHoraConsulta: OffsetDateTime,

    @Column(name = "id_medico", nullable = false)
    var idMedico: UUID,

    @Column(name = "id_paciente", nullable = false)
    var idPaciente: UUID,

    @Column(nullable = false, length = 20)
    var status: String = "AGENDADA",

    // justificativa opcional; só será preenchida quando cancelar
    @Column(name = "justificativa_cancelamento", length = 300, nullable = true)
    var justificativaCancelamento: String? = null,

    @Column(nullable = false)
    var dataCadastro: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var dataUltimaAtualizacao: OffsetDateTime = OffsetDateTime.now(),

    @Column(length = 80)
    var usuarioUltimaAtualizacao: String? = null
)