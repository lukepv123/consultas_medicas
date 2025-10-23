package curso.petenusso.clinicsapp.api.prontuarios.dto

data class ProntuarioListResponse(

    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int,
    val data: List<ProntuarioDTO>

)
