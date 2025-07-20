package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.runtime.Stable
import pl.techbrewery.sam.PageState
import pl.techbrewery.sam.kmp.database.entity.SingleItem

@Stable
data class ShoppingListScreenState(
    val items: List<SingleItem> = emptyList()
): PageState
