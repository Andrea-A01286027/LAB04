package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.sabores.data.remote.Network
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.Review
import mx.tec.sabores.ui.components.RatingLabel
import mx.tec.sabores.ui.components.StarsRow
import mx.tec.sabores.ui.theme.SaboresTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    summary: RatingSummary,
    reviews: List<Review>,
    onWriteReviewClick: () -> Unit,
    onBack: () -> Unit,
    onEditReviewClick: (Review) -> Unit = {},
    onDeleteReviewClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(restaurant.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onWriteReviewClick,
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                text = { Text("Escribir reseña") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(restaurant.emoji, style = MaterialTheme.typography.displayLarge)
                Text(
                    "${restaurant.cuisine} · ${restaurant.priceLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                RatingLabel(summary)
                Spacer(Modifier.height(12.dp))
                Text(restaurant.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(restaurant.address, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(20.dp))
                Text("Reseñas", style = MaterialTheme.typography.titleLarge)
            }

            if (reviews.isEmpty()) {
                item {
                    Text(
                        "Nadie ha reseñado este lugar. Sé la primera persona.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reviews) { review ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            StarsRow(review.stars)
                            Spacer(Modifier.height(6.dp))
                            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                            if (review.author == Network.alumno) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { onEditReviewClick(review) }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = { onDeleteReviewClick(review.id) }) {
                                        Text("Borrar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailPreview() {
    SaboresTheme {
        RestaurantDetailScreen(
            restaurant = Restaurant(
                1, "La Chinampa", "Mexicana", "Av. Garza Sada 300",
                "Cocina de mercado: tacos de guisado, sopes y agua del día.", 1, "🌮"
            ),
            summary = RatingSummary(4.5, 2),
            reviews = listOf(
                Review(1, 1, "a01286027", 5, "Excelente comida y atención"),
                Review(2, 1, "a01234567", 4, "Muy ricos los tacos")
            ),
            onWriteReviewClick = {},
            onBack = {}
        )
    }
}
