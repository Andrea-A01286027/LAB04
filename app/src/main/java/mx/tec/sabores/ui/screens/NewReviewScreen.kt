package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.ReviewError
import mx.tec.sabores.domain.ReviewValidator
import mx.tec.sabores.ui.components.StarPicker
import mx.tec.sabores.ui.state.NewReviewUiState
import mx.tec.sabores.ui.theme.SaboresTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReviewScreen(
    restaurant: Restaurant,
    uiState: NewReviewUiState,
    onStarsChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Reseñar ${restaurant.name}") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Cómo estuvo?", style = MaterialTheme.typography.titleMedium)
            StarPicker(value = uiState.stars, onValueChange = onStarsChange)
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = onCommentChange,
                label = { Text("Tu reseña") },
                minLines = 4,
                isError = uiState.commentError != null,
                supportingText = {
                    val error = uiState.commentError
                    if (error != null) Text(error.message())
                    else Text("Te quedan ${uiState.charactersLeft} caracteres")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSave,
                enabled = uiState.canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Publicar reseña") }
        }
    }
}

private fun ReviewError.message(): String = when (this) {
    ReviewError.NoStars -> "Selecciona de 1 a 5 estrellas"
    ReviewError.CommentTooShort -> "Escribe al menos ${ReviewValidator.COMMENT_MIN} caracteres"
    ReviewError.CommentTooLong -> "Máximo ${ReviewValidator.COMMENT_MAX} caracteres"
}

@Preview(showBackground = true)
@Composable
private fun NewReviewPreview() {
    SaboresTheme {
        NewReviewScreen(
            restaurant = Restaurant(
                1, "La Chinampa", "Mexicana", "Av. Garza Sada 300",
                "Cocina de mercado: tacos de guisado, sopes y agua del día.", 1, "🌮"
            ),
            uiState = NewReviewUiState(stars = 4, comment = "Muy rica la comida"),
            onStarsChange = {},
            onCommentChange = {},
            onSave = {},
            onCancel = {}
        )
    }
}
