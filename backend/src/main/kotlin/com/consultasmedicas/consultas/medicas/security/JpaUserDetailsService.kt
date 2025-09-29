package com.consultasmedicas.consultas.medicas.security

import com.consultasmedicas.consultas.medicas.repository.UserRepository
import com.consultasmedicas.consultas.medicas.repository.UserRoleRepository
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class JpaUserDetailsService(
    private val users: UserRepository,
    private val roles: UserRoleRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val u = users.findByUsername(username.lowercase())
            ?: throw UsernameNotFoundException("User não encontrado")
        if (!u.enabled) throw DisabledException("Conta desabilitada")
        if (u.locked) throw LockedException("Conta bloqueada")
        val authorities = roles.findByUserId(u.id!!)
            .map { SimpleGrantedAuthority("ROLE_${it.role}") }
        return org.springframework.security.core.userdetails.User
            .withUsername(u.username)
            .password(u.passwordHash)
            .authorities(authorities)
            .build()
    }
}
