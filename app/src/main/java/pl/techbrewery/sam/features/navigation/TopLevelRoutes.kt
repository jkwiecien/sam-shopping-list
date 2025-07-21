package pl.techbrewery.sam.features.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import kotlinx.serialization.Serializable
import pl.techbrewery.sam.R
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon

//data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: HeterogeneousVectorIcon)
@Serializable
sealed class TopLevelScreen(val route: String) {
    @Serializable
    object ShoppingList: TopLevelScreen("shopping_list")
    @Serializable
    object Recipes: TopLevelScreen("recipes")
    @Serializable
    object Stores: TopLevelScreen("stores")
    @Serializable
    object Settings: TopLevelScreen("settings")
}

data class TopLevelRoute(
    val title: String,
    val icon: HeterogeneousVectorIcon,
    val route: String
)

val navigationTopLevelRoutes: List<TopLevelRoute>
    get() = listOf(
        TopLevelRoute(
            "List",
            HeterogeneousVectorIcon.VectorIcon(Icons.AutoMirrored.Filled.List),
            TopLevelScreen.ShoppingList.route
        ),
        TopLevelRoute(
            "Recipes",
            HeterogeneousVectorIcon.PainterIcon(R.drawable.ic_recipe_book_24dp),
            TopLevelScreen.Recipes.route
        ),
        TopLevelRoute(
            "Recipes",
            HeterogeneousVectorIcon.PainterIcon(R.drawable.ic_store_24dp),
            TopLevelScreen.Stores.route
        ),
        TopLevelRoute(
            "Recipes",
            HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Settings),
            TopLevelScreen.Settings.route
        ),
    )
