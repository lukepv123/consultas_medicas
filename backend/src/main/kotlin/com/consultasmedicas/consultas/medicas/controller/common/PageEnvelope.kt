package com.consultasmedicas.consultas.medicas.controller.common


import org.springframework.data.domain.Page

data class PageEnvelope<T>(
    val current_page: Int,
    val total_pages: Int,
    val total_items: Long,
    val per_page: Int,
    val data: List<T>
)

fun <T, R> Page<T>.toContractEnvelope(mapper: (T) -> R) =
    PageEnvelope(
        current_page = number + 1,
        total_pages = totalPages,
        total_items = totalElements,
        per_page = size,
        data = content.map(mapper)
    )