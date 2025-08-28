package pl.techbrewery.sam.kmp.model

import androidx.compose.runtime.Stable
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem

@Stable
data class ShoppingItemWithWeight(
    val shoppingListItem: ShoppingListItem,
    val indexWeight: IndexWeight
) {
    val itemId: Long get() = shoppingListItem.id
    val itemName: String get() = shoppingListItem.itemName
    val listId: Long get() = shoppingListItem.listId
    val checkedOff: Boolean get() = shoppingListItem.checkedOff
    val cloudId: String? get() = shoppingListItem.cloudId
    val weight: Long get() = indexWeight.weight
    val storeId: Long get() = indexWeight.storeId
    val indexWeightId: Long get() = indexWeight.id
}
