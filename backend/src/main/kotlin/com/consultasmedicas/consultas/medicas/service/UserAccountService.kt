package com.consultasmedicas.consultas.medicas.service

import com.consultasmedicas.consultas.medicas.model.*
import com.consultasmedicas.consultas.medicas.repository.UserRepository
import com.consultasmedicas.consultas.medicas.repository.UserRoleRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserAccountService(
    private val users: UserRepository,
    private val roles: UserRoleRepository,
    private val encoder: PasswordEncoder
) {
    //adm

    @Transactional
    fun createAdminIfNone(email: String, rawPassword: String): UUID {
        // Já existe ADMIN? bloqueia
        if (roles.existsByRole("ADMIN")) {
            throw IllegalStateException("Já existe um ADMIN cadastrado")
        }

        val normalized = email.trim().lowercase()
        val user = if (users.existsByUsername(normalized)) {
            users.findByUsername(normalized)!! // reaproveita conta, se já existir
        } else {
            users.save(
                User(
                    username = normalized,
                    passwordHash = encoder.encode(rawPassword),
                    enabled = true,
                    locked = false
                )
            )
        }

        roles.save(UserRole(user = user, role = "ADMIN"))
        return user.id!!
    }






    @Transactional
    fun createForMedico(email: String, rawPassword: String, medico: Medico): User {
        if (users.existsByUsername(email.lowercase())) {
            throw IllegalStateException("E-mail já cadastrado")
        }
        val u = User(
            username = email.lowercase(),
            passwordHash = encoder.encode(rawPassword),
            medico = medico
        )
        val saved = users.save(u)
        roles.save(UserRole(user = saved, role = "MEDICO"))
        return saved
    }

    @Transactional
    fun createForPaciente(email: String, rawPassword: String, paciente: Paciente): User {
        if (users.existsByUsername(email.lowercase())) {
            throw IllegalStateException("E-mail já cadastrado")
        }
        val u = User(
            username = email.lowercase(),
            passwordHash = encoder.encode(rawPassword),
            paciente = paciente
        )
        val saved = users.save(u)
        roles.save(UserRole(user = saved, role = "PACIENTE"))
        return saved
    }


}