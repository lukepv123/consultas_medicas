package curso.petenusso.clinicsapp.core

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val throwable: Throwable) : AppResult<Nothing>()
}

inline fun <T> runCatchingResult(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (t: Throwable) {
        AppResult.Error(t)
    }
