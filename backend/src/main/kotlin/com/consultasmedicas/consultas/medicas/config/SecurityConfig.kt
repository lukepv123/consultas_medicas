package com.consultasmedicas.consultas.medicas.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun users(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin = User.withUsername("admin").password(passwordEncoder.encode("admin123")).roles("ADMIN").build()
        val operador = User.withUsername("operador").password(passwordEncoder.encode("operador123")).roles("OPERADOR").build()
        val medico = User.withUsername("medico").password(passwordEncoder.encode("medico123")).roles("MEDICO").build()
        return InMemoryUserDetailsManager(admin, operador, medico)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests {
                // S1: médicos
                it.requestMatchers("/medicos/**").hasAnyRole("ADMIN", "OPERADOR")
                // S2: pacientes
                it.requestMatchers(HttpMethod.POST, "/pacientes").hasAnyRole("ADMIN", "OPERADOR")
                it.requestMatchers(HttpMethod.GET, "/pacientes/**").hasAnyRole("ADMIN", "OPERADOR", "MEDICO")
                // S2: consultas

                it.requestMatchers(HttpMethod.POST, "/consultas").hasAnyRole("ADMIN", "OPERADOR")
                it.requestMatchers(HttpMethod.GET, "/consultas/paciente/**").hasAnyRole("ADMIN", "OPERADOR")
                it.requestMatchers(HttpMethod.GET, "/consultas/medico/**").hasAnyRole("ADMIN", "OPERADOR", "MEDICO")




                // S3: cancelamento — apenas ADMIN
                it.requestMatchers(HttpMethod.POST, "/consultas/cancelamento").hasRole("ADMIN")



                // S3: cancelamento — apenas ADMIN
                it.requestMatchers(HttpMethod.POST, "/consultas/cancelamento").hasRole("ADMIN")

                // S3: prontuários
                it.requestMatchers(HttpMethod.POST, "/prontuarios").hasRole("MEDICO")
                it.requestMatchers(HttpMethod.GET, "/prontuarios/paciente/**").hasAnyRole("MEDICO")



                it.anyRequest().authenticated()




            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                it.accessDeniedHandler(jsonAccessDeniedHandler())
            }
            .headers { it.frameOptions { fo -> fo.disable() } }

        return http.build()
    }

    @Bean
    fun jsonAccessDeniedHandler(): AccessDeniedHandler =
        AccessDeniedHandler { _, response, _ ->
            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = "application/json"
            response.writer.write("""{"status":403,"error":"FORBIDDEN","message":"Acesso negado."}""")
            response.writer.flush()
        }
}
