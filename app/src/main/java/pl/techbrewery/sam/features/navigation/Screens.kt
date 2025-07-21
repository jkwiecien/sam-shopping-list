package pl.techbrewery.sam.features.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import kotlinx.serialization.Serializable
import pl.techbrewery.sam.R
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon

@Serializable
sealed class Screen(val route: String) {
    @Serializable
    object ShoppingList: Screen(ScreenRoute.ShoppingList)
    @Serializable
    object Recipes: Screen(ScreenRoute.Recipes)
    @Serializable
    object Stores: Screen(ScreenRoute.Stores)
    @Serializable
    object Settings: Screen(ScreenRoute.Settings)
    object StoreEditor: Screen(ScreenRoute.StoreEditor)
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
