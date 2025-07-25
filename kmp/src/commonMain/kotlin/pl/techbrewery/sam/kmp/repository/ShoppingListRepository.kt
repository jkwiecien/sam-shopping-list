package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.di.kmpModules
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT
import pl.techbrewery.sam.kmp.utils.tempLog

class ShoppingListRepository(
    private val db: KmpDatabase
) {
    val singleItemDao get() = db.singleItemDao()
    val storeDao get() = db.storeDao()

    suspend fun addItemToShoppingList(itemName: String, indexWeight: Long) {
        val selectedStore = storeDao.getSelectedStore()!!
        val existingItem = singleItemDao.getSingleItemByName(itemName, selectedStore.storeId)
        if (existingItem != null) {
            singleItemDao.updateSingleItem(existingItem.copy(checkedOff = false))
        } else {
            val item = SingleItem(
                storeId = selectedStore.storeId,
                itemName = itemName.lowercase(),
                checkedOff = false,
                indexWeight = indexWeight
            )
            singleItemDao.insertSingleItem(item)
        }
    }

    suspend fun checkOffItem(itemId: Long) {
        singleItemDao.getSingleItemById(itemId)?.let { item ->
            singleItemDao.updateSingleItem(item.copy(checkedOff = true))
        }
    }

    suspend fun getAllItems(): List<SingleItem> {
        return singleItemDao.getAllSingleItems()
    }

    suspend fun getItemsForSelectedStore(): List<SingleItem> {
        return storeDao.getSelectedStore()?.let { selectedStore ->
            singleItemDao.getAllSingleItemsForStore(selectedStore.storeId)
        } ?: emptyList()
    }

    fun getAllItemsFlow(): Flow<List<SingleItem>> {
        return singleItemDao.getAllSingleItemsFlow()
    }

    suspend fun updateItems(items: List<SingleItem>) {
        tempLog("Updating items with ${items.joinToString { "${it.itemName}:${it.indexWeight}" }}")
        items.forEach { item ->
            singleItemDao.updateSingleItem(item)
        }
    }

    suspend fun updateItem(item: SingleItem) {
        singleItemDao.updateSingleItem(item)
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
                }.sortedByDescending { it.indexWeight }
            if (updatedItems.groupBy { it.indexWeight }.any { it.value.size >= 2 }) {
                updatedItems = reIndexWeights(updatedItems)
            }
            updatedItems
        }

    private fun reIndexWeights(
        items: List<SingleItem>
    ): List<SingleItem> {
        return items
            .sortedByDescending { it.indexWeight }
            .mapIndexed { index, item ->
                val newWeight = (items.size - index + 1) * DEFAULT_INDEX_GAP
                item.copy(indexWeight = newWeight)
            }
    }

    fun getSuggestedItems(
        storeId: Long,
        query: String
    ): Flow<List<SingleItem>> {
        if (query.isBlank()) {
            return flowOf(emptyList())
        } else {
            return singleItemDao.getSuggestedItemsFlow(query).take(5)
        }
    }


}