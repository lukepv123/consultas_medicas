package curso.petenusso.clinicsapp.api.consulta.dto

data class ConsultaListResponse(
    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int,
    val data: List<ConsultaDTO>
)
