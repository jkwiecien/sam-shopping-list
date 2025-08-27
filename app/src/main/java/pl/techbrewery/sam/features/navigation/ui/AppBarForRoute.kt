package pl.techbrewery.sam.features.navigation.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.techbrewery.sam.features.shoppinglist.ShareShoppingListPressed
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.screen_title_recipe_editor
import pl.techbrewery.sam.resources.screen_title_recipes
import pl.techbrewery.sam.resources.screen_title_settings
import pl.techbrewery.sam.resources.screen_title_shopping_list
import pl.techbrewery.sam.resources.screen_title_store_editor
import pl.techbrewery.sam.resources.screen_title_stores
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.statusBarAsTopPadding

@Composable
fun AppBarForRoute(
    route: String,
    canNavigateBack: Boolean,
    hideBar: Boolean = false,
    onNavigateUp: () -> Unit = {},
    onAction: (Any) -> Unit = {}
) {
    val screenTitle = when (route) {
        ScreenRoute.ShoppingList -> stringResource(Res.string.screen_title_shopping_list)
        ScreenRoute.Recipes -> stringResource(Res.string.screen_title_recipes)
        ScreenRoute.Stores -> stringResource(Res.string.screen_title_stores)
        ScreenRoute.Settings -> stringResource(Res.string.screen_title_settings)
        ScreenRoute.StoreEditor -> stringResource(Res.string.screen_title_store_editor)
        ScreenRoute.RecipeEditor -> stringResource(Res.string.screen_title_recipe_editor)
        else -> "" // Or handle unknown routes appropriately
    }
    val height: Dp =
        if (hideBar) 0.dp else AppBarHeight //fixme as it glitched with full screen list
    val modifier = Modifier.Companion
        .animateContentSize(animationSpec = tween(durationMillis = 300))
        .fillMaxWidth().let {
            if (height > 0.dp) {
                it.height(height + statusBarAsTopPadding())
            } else {
                it
                    .statusBarsPadding()
                    .height(height)
            }
        }
    AppBar(
        title = screenTitle,
        canNavigateBack = canNavigateBack,
        modifier = modifier,
        navigateUp = onNavigateUp,
        actions = { ActionsForRoute(route, onAction) }
    )
}

@Composable
private fun ActionsForRoute(
    route: String,
    onAction: (Any) -> Unit = {}
) {
    tempLog("Drawing actions")
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (route == ScreenRoute.ShoppingList) {
            tempLog("Share action drawn")
            IconButton(
                onClick = { onAction(ShareShoppingListPressed) }
            ) {
                Icon(Icons.Outlined.Share, contentDescription = "Share shopping list")
            }
        }
    }
}