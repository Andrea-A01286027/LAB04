package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.ui.components.RestaurantCard
import mx.tec.sabores.ui.theme.SaboresTheme

@Composable
fun RestaurantListScreen(
    restaurants: List<Restaurant>,
    summaryOf: (Int) -> RatingSummary,
    onRestaurantClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (restaurants.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No hay restaurantes que mostrar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(restaurants, key = { it.id }) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                summary = summaryOf(restaurant.id),
                onClick = { onRestaurantClick(restaurant.id) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListPreview() {
    SaboresTheme {
        RestaurantListScreen(
            restaurants = RestaurantRepository().getAll(),
            summaryOf = { RatingSummary(4.2, 3) },
            onRestaurantClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyListPreview() {
    SaboresTheme {
        RestaurantListScreen(
            restaurants = emptyList(),
            summaryOf = { RatingSummary(0.0, 0) },
            onRestaurantClick = {}
        )
    }
}
