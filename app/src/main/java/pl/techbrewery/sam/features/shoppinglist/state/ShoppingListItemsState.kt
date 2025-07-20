package pl.techbrewery.sam.features.shoppinglist.state

import androidx.compose.runtime.Stable
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
internal data class ShoppingListItemsState(
    val items: ImmutableList<SingleItem> = emptyList<SingleItem>().toImmutableList()
) {
    constructor(items: List<SingleItem>) : this(items.toList().toImmutableList())
}
