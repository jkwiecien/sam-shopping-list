package pl.techbrewery.sam.features.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import kotlinx.serialization.Serializable
import pl.techbrewery.sam.R
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon

//data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: HeterogeneousVectorIcon)
@Serializable
sealed class Screen(val route: String) {
    @Serializable
    object ShoppingList: Screen("shopping_list")
    @Serializable
    object Recipes: Screen("recipes")
    @Serializable
    object Stores: Screen("stores")
    @Serializable
    object Settings: Screen("settings")

    @Serializable
    object StoreEditor: Screen("store_editor")
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
            Screen.ShoppingList.route
        ),
        TopLevelRoute(
            "Recipes",
            HeterogeneousVectorIcon.PainterIcon(R.drawable.ic_recipe_book_24dp),
            Screen.Recipes.route
        ),
        TopLevelRoute(
            "Stores",
            HeterogeneousVectorIcon.PainterIcon(R.drawable.ic_store_24dp),
            Screen.Stores.route
        ),
        TopLevelRoute(
            "Settings",
            HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Settings),
            Screen.Settings.route
        ),
    )
