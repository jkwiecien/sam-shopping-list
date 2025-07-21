package pl.techbrewery.sam.kmp.repository

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.screen_title_recipes
import pl.techbrewery.sam.resources.screen_title_settings
import pl.techbrewery.sam.resources.screen_title_shopping_list
import pl.techbrewery.sam.resources.screen_title_store_editor
import pl.techbrewery.sam.resources.screen_title_stores

class LocalizedResources {

    @Composable
    fun getScreenTitle(route: String?): String {
        return when (route) {
            ScreenRoute.ShoppingList -> stringResource(Res.string.screen_title_shopping_list)
            ScreenRoute.Recipes -> stringResource(Res.string.screen_title_recipes)
            ScreenRoute.Stores -> stringResource(Res.string.screen_title_stores)
            ScreenRoute.Settings -> stringResource(Res.string.screen_title_settings)
            ScreenRoute.StoreEditor -> stringResource(Res.string.screen_title_store_editor)
            else -> "" // Or handle unknown routes appropriately
        }
    }
}