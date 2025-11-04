package com.consultasmedicas.consultas.medicas.repository


import com.consultasmedicas.consultas.medicas.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?

    @Query("select u from User u where u.medico.id in :ids")
    fun findByMedicoIdIn(ids: List<UUID>): List<User>

    @Query("select u from User u where u.medico.id = :id")
    fun findByMedicoId(id: UUID): List<User>

    fun existsByUsername(username: String): Boolean

    // UserRepository.kt
    @Query("""
    select u from User u
    left join fetch u.medico m
    left join fetch u.paciente p
    where lower(u.username) = lower(:username)
""")
    fun findByUsernameWithLinks(username: String): User?


    @Query("""
        select u.username 
          from User u 
         where u.paciente.id = :pacienteId
    """)
    fun findEmailByPacienteId(@Param("pacienteId") pacienteId: UUID): String?

}