package com.consultasmedicas.consultas.medicas.controller

import com.consultasmedicas.consultas.medicas.controller.dto.users.SetupAdminDTO
import com.consultasmedicas.consultas.medicas.repository.UserRoleRepository
import com.consultasmedicas.consultas.medicas.service.UserAccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
@RequestMapping("/setup")
class SetupController (private val accounts: UserAccountService,
    private val roles: UserRoleRepository) {


    @GetMapping("/status")
    @jakarta.annotation.security.PermitAll
    fun status() = mapOf("hasAdmin" to roles.existsByRole("ADMIN"))


    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @jakarta.annotation.security.PermitAll
    fun criarAdmin(@Valid @RequestBody body: SetupAdminDTO): Map<String, Any> =
        try {
            mapOf("id" to accounts.createAdminIfNone(body.email, body.senha))
        } catch (e: IllegalStateException){
            throw ResponseStatusException(HttpStatus.CONFLICT, e.message)
        }


}