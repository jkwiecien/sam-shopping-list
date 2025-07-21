package pl.techbrewery.sam.features.shoppinglist.state

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.kmp.database.entity.SingleItem

@Stable
internal data class ShoppingListItemsState(
    val items: ImmutableList<SingleItem> = emptyList<SingleItem>().toImmutableList()
) {
    constructor(items: List<SingleItem>) : this(items.toList().toImmutableList())
}