package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.SingleItem

class ShoppingListRepository(
    private val db: KmpDatabase
) {
    suspend fun insertItem(itemName: String, indexWeight: Float) {
        val item = SingleItem(
            itemName = itemName.lowercase(),
            indexWeight = indexWeight
        )
        db.singleItemDao().insertSingleItem(item)
    }

    suspend fun checkOffItem(itemName: String) {
        db.singleItemDao().getSingleItemByName(itemName)?.let { item ->
            db.singleItemDao().updateSingleItem(item.copy(checkedOff = true))
        }
    }

    fun getLastShoppingList(): Flow<List<SingleItem>> {
        return db.singleItemDao().getUncheckedSingleItems()
    }

    suspend fun  updateItems(items: List<SingleItem>) {
        items.forEach { item ->
            db.singleItemDao().updateSingleItem(item)
        }
    }

    suspend fun updateItem(item: SingleItem) {
        db.singleItemDao().updateSingleItem(item)
    }
}