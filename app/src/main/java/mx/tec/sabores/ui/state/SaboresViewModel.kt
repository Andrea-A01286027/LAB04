package mx.tec.sabores.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.RestaurantEnLista
import mx.tec.sabores.domain.Review
import retrofit2.HttpException
import java.io.IOException

data class MyReviewItem(val restaurantName: String, val review: Review)

/** El restaurante y sus reseñas, que la pantalla de detalle necesita juntos. */
data class Detalle(
    val restaurant: Restaurant,
    val reviews: List<Review>
) {
    // La regla del dominio sigue viva: el promedio se calcula aquí, no se hereda
    // del servidor, para que cambie al instante al publicar tu reseña.
    val summary: RatingSummary = RatingSummary.from(reviews)
}

class SaboresViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    var restaurantes by mutableStateOf<UiState<List<RestaurantEnLista>>>(UiState.Cargando)
        private set

    var detalle by mutableStateOf<UiState<Detalle>>(UiState.Cargando)
        private set

    var misResenas by mutableStateOf<UiState<List<MyReviewItem>>>(UiState.Cargando)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    init {
        cargarRestaurantes()
    }

    fun cargarRestaurantes() {
        viewModelScope.launch {
            restaurantes = UiState.Cargando
            restaurantes = pedir { repository.getAllForList() }
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            detalle = UiState.Cargando
            detalle = pedir { Detalle(repository.getById(id), repository.getReviews(id)) }
        }
    }

    fun cargarMisResenas() {
        viewModelScope.launch {
            misResenas = UiState.Cargando
            misResenas = pedir {
                val reviews = repository.getMyReviews()
                val restaurants = repository.getAll().associateBy { it.id }
                reviews.map { review ->
                    MyReviewItem(
                        restaurantName = restaurants[review.restaurantId]?.name ?: "Restaurante",
                        review = review
                    )
                }
            }
        }
    }

    fun borrarResena(id: Int, alTerminar: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val borrado = repository.deleteReview(id)
                if (!borrado) {
                    mensajeError = "Esa reseña no es tuya."
                } else {
                    cargarMisResenas()
                    alTerminar?.invoke()
                }
            } catch (e: IOException) {
                mensajeError = "No hay conexión. Revisa tu internet."
            } catch (e: HttpException) {
                mensajeError = mensajeDe(e)
            }
        }
    }

    fun editarResena(id: Int, stars: Int? = null, comment: String? = null, alTerminar: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                repository.editReview(id, stars, comment)
                cargarMisResenas()
                alTerminar?.invoke()
            } catch (e: IOException) {
                mensajeError = "No hay conexión. Revisa tu internet."
            } catch (e: HttpException) {
                mensajeError = mensajeDe(e)
            }
        }
    }

    fun limpiarMensaje() {
        mensajeError = null
    }

    private suspend fun <T> pedir(block: suspend () -> T): UiState<T> = try {
        UiState.Exito(block())
    } catch (e: IOException) {
        UiState.Error("No hay conexión. Revisa tu internet.")
    } catch (e: HttpException) {
        UiState.Error(mensajeDe(e))
    }
}
