package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT

class ShoppingListRepository(
    private val db: KmpDatabase
) {
    suspend fun insertItem(itemName: String, indexWeight: Long) {
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

    fun getAllItems(): Flow<List<SingleItem>> {
        return db.singleItemDao().getAllSingleItemsFlow()
    }

    suspend fun updateItems(items: List<SingleItem>) {
        items.forEach { item ->
            db.singleItemDao().updateSingleItem(item)
        }
    }

    suspend fun updateItem(item: SingleItem) {
        db.singleItemDao().updateSingleItem(item)
    }

    suspend fun moveItem(from: Int, to: Int, currentItems: List<SingleItem>): List<SingleItem> =
        coroutineScope {
            if (from == to) currentItems
            val itemMoved = currentItems[from]
            val itemReplaced = currentItems[to]
            val goingUp = itemMoved.indexWeight < itemReplaced.indexWeight
            val newIndexWeight = if (goingUp) {
                itemReplaced.indexWeight + INDEX_INCREMENT
            } else {
                itemReplaced.indexWeight - INDEX_INCREMENT
            }

            var updatedItems = currentItems
                .map { item ->
                    //Safely entering new weight after potential reindexing
                    if (item.itemName == itemMoved.itemName) {
                        item.copy(indexWeight = newIndexWeight)
                    } else {
                        item
                    }
                }
            if (updatedItems.groupBy { it.indexWeight }.any { it.value.size >= 2 }) {
                updatedItems = reIndexWeights(updatedItems)
            }
            updateItems(updatedItems)
            updatedItems
        }

    private fun reIndexWeights(
        items: List<SingleItem>
    ): List<SingleItem> {
        return items
            .sortedBy { it.indexWeight }
            .mapIndexed { index, item ->
                val newWeight = (index + 1) * DEFAULT_INDEX_GAP
                item.copy(indexWeight = newWeight)
            }
    }
}