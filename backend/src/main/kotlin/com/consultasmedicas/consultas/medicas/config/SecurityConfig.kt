package com.consultasmedicas.consultas.medicas.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests {
                // ÚNICA exceção aberta (sem login): cadastro de paciente
                it.requestMatchers(HttpMethod.GET,  "/setup/status").permitAll()
                it.requestMatchers(HttpMethod.POST, "/setup/admin").permitAll()
                it.requestMatchers(HttpMethod.POST, "/pacientes").permitAll()

                // (opcional) health/docs
                // it.requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                it.accessDeniedHandler { _, res, _ ->
                    res.status = HttpStatus.FORBIDDEN.value()
                    res.contentType = "application/json"
                    res.writer.write("""{"status":403,"error":"FORBIDDEN","message":"Acesso negado."}""")
                    res.writer.flush()
                }
            }
            .headers { it.frameOptions { fo -> fo.disable() } }

        return http.build()
    }
}



