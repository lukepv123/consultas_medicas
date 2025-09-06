package com.consultasmedicas.consultas.medicas.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class LoggingMDCFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)
        MDC.put("path", request.requestURI)
        MDC.put("method", request.method)

        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated) {
            MDC.put("user", auth.name)
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}