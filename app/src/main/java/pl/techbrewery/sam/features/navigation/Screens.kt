package pl.techbrewery.sam.features.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import org.jetbrains.compose.resources.StringResource
import pl.techbrewery.sam.R
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.tab_title_recipes
import pl.techbrewery.sam.resources.tab_title_settings
import pl.techbrewery.sam.resources.tab_title_shopping_list
import pl.techbrewery.sam.resources.tab_title_stores
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon


data class TopLevelRoute(
    val titleResource: StringResource,
    val icon: HeterogeneousVectorIcon,
    val route: String
)

object NavigationTopLevelRoutes {
    val routes: List<TopLevelRoute> =
        listOf(
            TopLevelRoute(
                Res.string.tab_title_shopping_list,
                HeterogeneousVectorIcon.VectorIcon(Icons.AutoMirrored.Filled.List),
                ScreenRoute.ShoppingList
            ),
            TopLevelRoute(
                Res.string.tab_title_recipes,
                HeterogeneousVectorIcon.PainterIcon(R.drawable.ic_recipe_book_24dp),
                ScreenRoute.Recipes
            ),
            TopLevelRoute(
                Res.string.tab_title_stores,
                HeterogeneousVectorIcon.PainterIcon(R.drawable.ic_store_24dp),
                ScreenRoute.Stores
            ),
            TopLevelRoute(
                Res.string.tab_title_settings,
                HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Settings),
                ScreenRoute.Settings
            ),
        )
}
