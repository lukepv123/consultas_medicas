package com.consultasmedicas.consultas.medicas.model
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import java.time.OffsetDateTime
import java.util.*
// Paciente.kt
@Entity
@Table(name = "pacientes", uniqueConstraints = [UniqueConstraint(name="uk_paciente_cpf", columnNames=["cpf"])])
class Paciente(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 14)
    var cpf: String,

    @Column(nullable = false, length = 120)
    var nome: String,

    @Column(nullable = false)
    var dataCadastro: java.time.OffsetDateTime = java.time.OffsetDateTime.now(),

    @Column(nullable = false)
    var dataUltimaAtualizacao: java.time.OffsetDateTime = java.time.OffsetDateTime.now(),

    @Column(length = 80)
    var usuarioUltimaAtualizacao: String? = null
)