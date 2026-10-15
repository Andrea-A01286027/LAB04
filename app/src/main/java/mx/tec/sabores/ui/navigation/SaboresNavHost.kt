package mx.tec.sabores.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.tec.sabores.domain.Review
import mx.tec.sabores.domain.ReviewValidator
import mx.tec.sabores.ui.components.CargandoView
import mx.tec.sabores.ui.components.ErrorView
import mx.tec.sabores.ui.components.StarPicker
import mx.tec.sabores.ui.screens.MyReviewsScreen
import mx.tec.sabores.ui.screens.NewReviewScreen
import mx.tec.sabores.ui.screens.RestaurantDetailScreen
import mx.tec.sabores.ui.screens.RestaurantListScreen
import mx.tec.sabores.ui.state.NewReviewViewModel
import mx.tec.sabores.ui.state.SaboresViewModel
import mx.tec.sabores.ui.state.UiState

@Composable
fun SaboresApp() {
    val nav = rememberNavController()
    val viewModel: SaboresViewModel = viewModel()

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = MenuItem.entries.any { it.route == currentRoute }

    var editingReview by remember { mutableStateOf<Review?>(null) }

    if (editingReview != null) {
        val review = editingReview!!
        var stars by remember(review) { mutableStateOf(review.stars) }
        var comment by remember(review) { mutableStateOf(review.comment) }

        AlertDialog(
            onDismissRequest = { editingReview = null },
            title = { Text("Editar reseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StarPicker(value = stars, onValueChange = { stars = it })
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Tu reseña") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editarResena(review.id, stars, comment) {
                            (viewModel.detalle as? UiState.Exito)?.datos?.restaurant?.id?.let {
                                viewModel.cargarDetalle(it)
                            }
                        }
                        editingReview = null
                    },
                    enabled = ReviewValidator.isValid(stars, comment)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReview = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val mensajeError = viewModel.mensajeError
    if (mensajeError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.limpiarMensaje() },
            title = { Text("Aviso") },
            text = { Text(mensajeError) },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarMensaje() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MenuItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo(Route.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Route.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.HOME) {
                when (val estado = viewModel.restaurantes) {
                    is UiState.Cargando -> CargandoView()
                    is UiState.Error -> ErrorView(
                        mensaje = estado.mensaje,
                        onReintentar = { viewModel.cargarRestaurantes() }
                    )
                    is UiState.Exito -> RestaurantListScreen(
                        restaurants = estado.datos,
                        onRestaurantClick = { id -> nav.navigate(Route.detail(id)) }
                    )
                }
            }

            composable(
                route = Route.DETAIL,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                LaunchedEffect(id) { viewModel.cargarDetalle(id) }

                when (val estado = viewModel.detalle) {
                    is UiState.Cargando -> CargandoView()
                    is UiState.Error -> ErrorView(
                        mensaje = estado.mensaje,
                        onReintentar = { viewModel.cargarDetalle(id) }
                    )
                    is UiState.Exito -> {
                        val detalle = estado.datos
                        RestaurantDetailScreen(
                            restaurant = detalle.restaurant,
                            summary = detalle.summary,
                            reviews = detalle.reviews,
                            onWriteReviewClick = { nav.navigate(Route.newReview(id)) },
                            onBack = { nav.popBackStack() },
                            onEditReviewClick = { review -> editingReview = review },
                            onDeleteReviewClick = { reviewId ->
                                viewModel.borrarResena(reviewId) {
                                    viewModel.cargarDetalle(id)
                                }
                            }
                        )
                    }
                }
            }

            composable(
                route = Route.NEW_REVIEW,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                val restaurant = (viewModel.detalle as? UiState.Exito)?.datos?.restaurant ?: return@composable
                val formViewModel: NewReviewViewModel = viewModel()
                NewReviewScreen(
                    restaurant = restaurant,
                    uiState = formViewModel.uiState,
                    onStarsChange = formViewModel::onStarsChange,
                    onCommentChange = formViewModel::onCommentChange,
                    onSave = {
                        formViewModel.publicar(id) { nav.popBackStack() }
                    },
                    onCancel = { nav.popBackStack() }
                )
            }

            composable(Route.MY_REVIEWS) {
                LaunchedEffect(Unit) {
                    viewModel.cargarMisResenas()
                }
                when (val estado = viewModel.misResenas) {
                    is UiState.Cargando -> CargandoView()
                    is UiState.Error -> ErrorView(
                        mensaje = estado.mensaje,
                        onReintentar = { viewModel.cargarMisResenas() }
                    )
                    is UiState.Exito -> MyReviewsScreen(
                        items = estado.datos,
                        onEdit = { review -> editingReview = review },
                        onDelete = { reviewId -> viewModel.borrarResena(reviewId) }
                    )
                }
            }
        }
    }
}
