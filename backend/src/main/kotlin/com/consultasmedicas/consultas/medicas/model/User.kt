package com.consultasmedicas.consultas.medicas.model

import jakarta.persistence.*
import java.util.UUID
@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(name="uk_users_username", columnNames=["username"])])
class User(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 160)
    var username: String, // e-mail de login

    @Column(nullable = false, length = 200)
    var passwordHash: String,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var locked: Boolean = false,

    // vínculos opcionais (um user pode representar um médico OU paciente, ou nenhum)
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "medico_id")
    var medico: Medico? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "paciente_id")
    var paciente: Paciente? = null
)