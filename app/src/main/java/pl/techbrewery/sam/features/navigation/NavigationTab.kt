package pl.techbrewery.sam.features.navigation

import androidx.compose.runtime.Stable

@Stable
enum class NavigationTab {
    SHOPPING_LIST, RECIPES, STORES, SETTINGS;

    fun index(): Int = when(this) {
        SHOPPING_LIST -> 0
        RECIPES -> 1
        STORES -> 2
        SETTINGS -> 3
    }
}