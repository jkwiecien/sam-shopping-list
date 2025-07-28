package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_item_already_in_recipe
import pl.techbrewery.sam.resources.error_item_already_in_shopping_list
import java.sql.SQLIntegrityConstraintViolationException

class ShoppingListRepository(
    private val db: KmpDatabase
) {
    private val singleItemDao get() = db.singleItemDao()
    private val storeDao get() = db.storeDao()
    private val shoppingListItemDao get() = db.shoppingListItemDao()

    suspend fun saveSearchResult(itemName: String) {
        singleItemDao.insertSingleItem(SingleItem(itemName = itemName))
    }

    suspend fun addItemToShoppingList(itemName: String, allShoppingListItems: List<ShoppingListItem>) {
        val allSingleItems = singleItemDao.getAllSingleItems()
        if (allShoppingListItems.any { allSingleItems.first{ si -> si.itemName == it.itemName}.itemName.lowercase() == itemName.lowercase() }) {
            throw SQLIntegrityConstraintViolationException(getString(Res.string.error_item_already_in_shopping_list))
        }
        val uncheckedItems = allShoppingListItems.filterNot { item -> item.checkedOff }

        val maxWeight = uncheckedItems.maxOfOrNull { it.indexWeight } ?: 0L
        val newWeight = maxWeight + DEFAULT_INDEX_GAP

        val selectedStore = storeDao.getSelectedStore()!!
        var singleItem = singleItemDao.getSingleItemByName(itemName)
        if (singleItem == null) {
            singleItem = SingleItem(itemName = itemName.lowercase())
            singleItemDao.insertSingleItem(singleItem)
        }

        val shoppingListItem =
            shoppingListItemDao.getShoppingListItem(selectedStore.storeId, singleItem.itemName)

        if (shoppingListItem != null) {
            shoppingListItemDao.update(shoppingListItem.copy(checkedOff = false))
        } else {
            shoppingListItemDao.insert(
                ShoppingListItem(
                    itemName = singleItem.itemName,
                    storeId = selectedStore.storeId,
                    indexWeight = newWeight,
                    checkedOff = false
                )
            )
        }
    }

    suspend fun checkOffItem(itemId: Long) {
        shoppingListItemDao.getShoppingListItem(itemId)?.let { item ->
            shoppingListItemDao.update(item.copy(checkedOff = true))
        }
    }

    suspend fun getAllItems(): List<SingleItem> {
        return singleItemDao.getAllSingleItems()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getShoppingListItemsForSelectedStore(): Flow<List<ShoppingListItem>> {
        return storeDao.getSelectedStoreFlow()
            .filterNotNull()
            .flatMapLatest { store ->
                shoppingListItemDao.getShoppingListItemsForStore(store.storeId) // Changed here
            }
    }

    fun getAllItemsFlow(): Flow<List<SingleItem>> {
        return singleItemDao.getAllSingleItemsFlow()
    }

    suspend fun updateItems(items: List<ShoppingListItem>) {
        items.forEach { item ->
            shoppingListItemDao.update(item)
        }
    }

    suspend fun moveItem(
        from: Int,
        to: Int,
        currentItems: List<ShoppingListItem>
    ): List<ShoppingListItem> =
        coroutineScope {
            if (from == to) return@coroutineScope currentItems

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
                    if (item.id == itemMoved.id) {
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
        items: List<ShoppingListItem>
    ): List<ShoppingListItem> {
        return items
            .sortedByDescending { it.indexWeight }
            .mapIndexed { index, item ->
                val newWeight = (items.size - index + 1) * DEFAULT_INDEX_GAP
                item.copy(indexWeight = newWeight)
            }
    }

    fun getSearchResults(query: String): Flow<List<SingleItem>> {
        return shoppingListItemDao.getSearchResults(query)
    }

    fun getSearchResults(query: String, exceptItems: List<String>): Flow<List<SingleItem>> {
        return shoppingListItemDao.getSearchResultsExcept(query, exceptItems)
    }

    suspend fun deleteItem(item: ShoppingListItem) {
        shoppingListItemDao.deleteById(item.id)
    }
}
