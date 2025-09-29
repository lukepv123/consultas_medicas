package com.consultasmedicas.consultas.medicas.model

import jakarta.persistence.*
import java.util.UUID

@Entity @Table(name = "user_roles")
class UserRole(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, length = 40)
    var role: String // "ADMIN","OPERADOR","MEDICO","PACIENTE"
)