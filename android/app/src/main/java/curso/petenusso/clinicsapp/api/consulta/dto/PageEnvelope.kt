package curso.petenusso.clinicsapp.api.consulta.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PageEnvelope<T>(
    @Json(name = "current_page") val currentPage: Int? = null,
    @Json(name = "total_pages")  val totalPages: Int? = null,
    @Json(name = "total_items")  val totalItems: Long? = null,
    @Json(name = "per_page")     val perPage: Int? = null,

    // Seu backend usa "data"
    @Json(name = "data")         val data: List<T>? = null,

    // Mantidos por compatibilidade com outros endpoints (se existirem)
    val content: List<T>? = null,
    val items:   List<T>? = null
) {
    fun list(): List<T> = data ?: items ?: content ?: emptyList()
}
