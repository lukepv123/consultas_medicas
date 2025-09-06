package com.consultasmedicas.consultas.medicas.model

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    name = "medicos",
    uniqueConstraints = [UniqueConstraint(name = "uk_medico_crm", columnNames = ["crm"])]
)
class Medico(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 20)
    var crm: String,

    @Column(nullable = false, length = 120)
    var nome: String,

    @Email
    @Column(nullable = false, length = 160)
    var email: String,

    @Column(nullable = false, length = 200)
    var senhaHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    var especialidade: Especialidade,

    @Column(nullable = false)
    var dataCadastro: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var dataUltimaAtualizacao: OffsetDateTime = OffsetDateTime.now(),

    @Column(length = 80)
    var usuarioUltimaAtualizacao: String? = null
)