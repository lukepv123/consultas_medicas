package curso.petenusso.clinicsapp.api.medico.dto

data class MedicoListResponse(

    val current_page: Int,
    val total_pages: Int,
    val total_items: Int,
    val per_page: Int,
    val data: List<MedicoDTO>


)
