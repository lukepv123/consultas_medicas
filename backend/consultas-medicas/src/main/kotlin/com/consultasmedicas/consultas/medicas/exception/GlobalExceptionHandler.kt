package com.consultasmedicas.consultas.medicas.exception

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.http.converter.HttpMessageNotReadableException

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val fields: Map<String, String?>? = null
)

@ControllerAdvice
class GlobalExceptionHandler {

    // 422 - Bean Validation em @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fields = ex.bindingResult.allErrors.associate { error ->
            val field = (error as? FieldError)?.field ?: error.objectName
            field to error.defaultMessage
        }
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiError(422, "UNPROCESSABLE_ENTITY", "Erro de validação", fields))
    }

    // 422 - Bean Validation em @RequestParam/@PathVariable (ex.: @Min em page/per_page)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ApiError> {
        val fields = ex.constraintViolations.associate { v ->
            // tenta extrair o nome do parâmetro/campo
            val path = v.propertyPath?.toString() ?: "param"
            path to v.message
        }
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiError(422, "UNPROCESSABLE_ENTITY", "Erro de validação", fields))
    }

    // 409 - Conflitos (índices únicos: CRM/CPF)
    @ExceptionHandler(DataIntegrityViolationException::class, IllegalStateException::class)
    fun handleConflict(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError(409, "CONFLICT", ex.message ?: "Conflito"))

    // 404 - Entidade não encontrada
    @ExceptionHandler(EntityNotFoundException::class, NoSuchElementException::class)
    fun handleNotFound(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(404, "NOT_FOUND", ex.message ?: "Recurso não encontrado"))

    // 404 - Rota inexistente (precisa de configs abaixo)
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandler(ex: NoHandlerFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(404, "NOT_FOUND", "Endpoint não encontrado: ${ex.requestURL}"))

    // 401 - Falhas de autenticação (qualquer AuthenticationException)
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuth(ex: AuthenticationException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError(401, "UNAUTHORIZED", ex.message ?: "Credenciais inválidas"))

    // 403 - Acesso negado (Spring Security usa AccessDeniedException)
    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(ex: AccessDeniedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError(403, "FORBIDDEN", ex.message ?: "Acesso negado"))

    // 400 - JSON malformado / tipo incorreto / parâmetro faltando / método/mediatype
    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        MismatchedInputException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
        HttpRequestMethodNotSupportedException::class,
        HttpMediaTypeNotSupportedException::class,
        IllegalArgumentException::class
    )
    fun handleBadRequest(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "BAD_REQUEST", ex.message ?: "Requisição inválida"))

    // Fallback - mantém 400 para qualquer outra não mapeada
    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "BAD_REQUEST", ex.message ?: "Requisição inválida"))
}
