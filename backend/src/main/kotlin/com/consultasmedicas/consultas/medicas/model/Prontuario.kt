package com.consultasmedicas.consultas.medicas.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "prontuarios")
class Prontuario(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var idPaciente: UUID,

    @Column(nullable = false, columnDefinition = "text")
    var atendimento: String,

    @Column(columnDefinition = "text")
    var alergias: String? = null,
    @Column(columnDefinition = "text")
    var deficiencia: String? = null,
    @Column(columnDefinition = "text")
    var comorbidade: String? = null,
    @Column(columnDefinition = "text")
    var exames: String? = null,
    @Column(columnDefinition = "text")
    var medicacao: String? = null,

    @Column(nullable = false)
    var dataCadastro: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var dataUltimaAtualizacao: OffsetDateTime = OffsetDateTime.now(),

    @Column(length = 80)
    var usuarioUltimaAtualizacao: String? = null
)