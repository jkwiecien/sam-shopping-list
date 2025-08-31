package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.cloud.CloudSyncService
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.model.ShoppingItemWithWeight
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.kmp.model.SuggestedItemType
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT
import pl.techbrewery.sam.kmp.utils.debugLog
import pl.techbrewery.sam.kmp.utils.errorLog
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.kmp.utils.warningLog
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_item_already_in_shopping_list
import java.sql.SQLIntegrityConstraintViolationException

private const val LOG_TAG = "ShoppingListRepository"

class ShoppingListRepository(
    private val db: KmpDatabase,
    private val syncService: CloudSyncService
) {
    private val singleItemDao get() = db.singleItemDao()
    private val shoppingListDao get() = db.shoppingListDao()
    private val shoppingListItemDao get() = db.shoppingListItemDao()
    private val recipesDao get() = db.recipeDao()
    private val indexWeightDao get() = db.indexWeightDao()
    private val storeDao get() = db.storeDao()

    suspend fun saveSearchResult(itemName: String): SingleItem = coroutineScope {
        val singleItem = SingleItem(itemName = itemName)
        singleItemDao.insertSingleItem(singleItem)
        syncService.cloudUpdater?.let { launch { it.saveSingleItem(singleItem) } }
        singleItem
    }

    suspend fun addSuggestedItemToShoppingList(suggestedItem: SuggestedItem) {
        when (suggestedItem.type) {
            SuggestedItemType.ITEM -> addItemToShoppingList(suggestedItem.itemName, false)
            SuggestedItemType.RECIPE -> {
                recipesDao.getRecipeWithItemsByName(suggestedItem.itemName)?.let { recipe ->
                    recipe.items.forEach { item -> addItemToShoppingList(item.itemName, false) }
                }
            }
        }
    }

    suspend fun addItemToShoppingList(itemName: String, throwErrorOnDuplicate: Boolean = true) {
        val selectedList = shoppingListDao.getShoppingList()!!
        val selectedStore = storeDao.getSelectedStore()!!
        val allSingleItems = singleItemDao.getAllSingleItems()
        val allShoppingItems = getShoppingListItemsForSelectedStore()
        if (allShoppingItems.any { itemWithWeight ->
                allSingleItems.first { si -> si.itemName == itemWithWeight.shoppingListItem.itemName }.itemName.lowercase() == itemName.lowercase()
            }
        ) {
            if (throwErrorOnDuplicate) throw SQLIntegrityConstraintViolationException(getString(Res.string.error_item_already_in_shopping_list))
            else return
        }
        val uncheckedItems = allShoppingItems.filterNot { data ->
            data.shoppingListItem.checkedOff
        }

        val maxWeight = uncheckedItems.maxOfOrNull { data ->
            data.indexWeight.weight
        } ?: 0L
        val newWeight = maxWeight + DEFAULT_INDEX_GAP

        var singleItem = singleItemDao.getSingleItemByName(itemName)
        if (singleItem == null) singleItem = saveSearchResult(itemName)

        var shoppingListItem =
            shoppingListItemDao.getShoppingListItem(selectedList.id, singleItem.itemName)

        if (shoppingListItem != null) {
            shoppingListItemDao.update(
                shoppingListItem.copy(
                    checkedOff = false,
                    updatedAt = getCurrentTime()
                )
            )
        } else {
            shoppingListItem = ShoppingListItem(
                itemName = singleItem.itemName,
                listId = selectedList.id,
                checkedOff = false
            )
            // Persist ShoppingListItem first to get its ID if needed, though not directly used for IndexWeight here
            val shoppingListItemId = shoppingListItemDao.insert(shoppingListItem)

            // Create and persist IndexWeight
            val newIndexWeight = IndexWeight(
                itemName = shoppingListItem.itemName,
                storeId = selectedStore.storeId,
                weight = newWeight
            )
            indexWeightDao.insert(newIndexWeight) // This ensures IndexWeight gets a proper ID
        }
    }

    suspend fun checkOffItem(itemId: Long) {
        shoppingListItemDao.getShoppingListItem(itemId)?.let { item ->
            shoppingListItemDao.update(item.copy(checkedOff = true, updatedAt = getCurrentTime()))
        }
    }

    suspend fun getAllItems(): List<SingleItem> {
        return singleItemDao.getAllSingleItems()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getShoppingListItemsForSelectedStoreFlow(): Flow<List<ShoppingItemWithWeight>> {
        return shoppingListDao.getSelectedListFlow()
            .filterNotNull()
            .flatMapLatest { list ->
                // Ensure store is fetched first to avoid multiple calls or null issues
                storeDao.getSelectedStoreFlow().filterNotNull().flatMapLatest { selectedStore ->
                    shoppingListItemDao.getShoppingListItemsFlow(list.id)
                        .map { items ->
                            items.map { item ->
                                var indexWeight = indexWeightDao.getIndexWeight(
                                    item.itemName,
                                    selectedStore.storeId
                                )
                                if (indexWeight == null) {
                                    // IndexWeight not found, create and persist it
                                    debugLog(
                                        "IndexWeight not found for ${item.itemName} in store ${selectedStore.storeId}. Creating new one.",
                                        LOG_TAG
                                    )
                                    val newPersistedIndexWeight = IndexWeight(
                                        itemName = item.itemName,
                                        storeId = selectedStore.storeId,
                                        // A sensible default weight, e.g., 0 or based on insertion time/order if needed
                                        // For simplicity, using 0, but this might need adjustment based on desired default ordering.
                                        // Or, fetch max weight and add DEFAULT_INDEX_GAP.
                                        // Let's keep it simple with 0 for now as reIndexWeights will fix it on move.
                                        weight = 0
                                    )
                                    val newId =
                                        indexWeightDao.insert(newPersistedIndexWeight) // This insert should be synchronous within this map if DAO is suspend or blocks
                                    indexWeight =
                                        newPersistedIndexWeight.copy(id = newId) // Use the object with the new ID
                                    debugLog(
                                        "Created and persisted IndexWeight for ${item.itemName} with new id $newId",
                                        LOG_TAG
                                    )
                                }
                                ShoppingItemWithWeight(item, indexWeight)
                            }.sortedByDescending { it.indexWeight.weight }
                        }
                }
            }.onEach {
                // This logging remains useful
                it.forEach { item ->
                    debugLog(
                        "Item from Flow: ${item.shoppingListItem.itemName}, IW.id: ${item.indexWeight.id}, IW.weight: ${item.indexWeight.weight}",
                        LOG_TAG
                    )
                }
            }
    }

    // You might need a similar adjustment for the non-Flow version if it's still used
    // in a way that affects the ReorderableItem state before the Flow updates.
    suspend fun getShoppingListItemsForSelectedStore(): List<ShoppingItemWithWeight> {
        val selectedStore = storeDao.getSelectedStore() ?: return emptyList()
        val storeId = selectedStore.storeId
        return shoppingListDao.getShoppingList()?.let { selectedList ->
            shoppingListItemDao.getShoppingListItemsForList(selectedList.id).map { item ->
                var indexWeight = indexWeightDao.getIndexWeight(item.itemName, storeId)
                if (indexWeight == null) {
                    debugLog(
                        "IndexWeight not found for ${item.itemName} in store ${storeId} (getShoppingListItemsForSelectedStore). Creating new one.",
                        LOG_TAG
                    )
                    val newPersistedIndexWeight = IndexWeight(
                        itemName = item.itemName,
                        storeId = storeId,
                        weight = 0 // Or other default
                    )
                    // Assuming insert returns the ID or you can query it back.
                    // For DAOs returning Long id:
                    val newId = indexWeightDao.insert(newPersistedIndexWeight)
                    indexWeight = newPersistedIndexWeight.copy(id = newId)
                    debugLog(
                        "Created and persisted IndexWeight for ${item.itemName} with new id $newId (getShoppingListItemsForSelectedStore)",
                        LOG_TAG
                    )
                }
                ShoppingItemWithWeight(item, indexWeight)
            }.sortedByDescending { it.indexWeight.weight }
        } ?: emptyList()
    }


    fun getAllItemsFlow(): Flow<List<SingleItem>> {
        return singleItemDao.getAllSingleItemsFlow()
    }

    suspend fun updateItems(items: List<ShoppingItemWithWeight>) = coroutineScope {
        // Ensure all index weights are persisted before attempting to update
        val itemsWithPersistedWeights = items.map {
            if (it.indexWeight.id == 0L) {
                debugLog(
                    "Attempting to update an item with transient IndexWeight: ${it.shoppingListItem.itemName}",
                    LOG_TAG
                )
                // This case should ideally be prevented by the getShoppingListItems...Flow modifications
                // However, as a safeguard, one might try to re-fetch/re-create here,
                // but it's better to fix it at the source (the Flow).
                // For now, let's assume the Flow provides persisted IndexWeights.
            }
            it
        }

        itemsWithPersistedWeights.forEach { (item, weight) ->
            // Defensive check: only update if id is not 0.
            if (weight.id != 0L) {
                val updatedItem = item.copy(updatedAt = getCurrentTime())
                val updatedIndexWeight = weight.copy(updatedAt = getCurrentTime())
                withContext(Dispatchers.Default) {
                    shoppingListItemDao.update(updatedItem)
                    indexWeightDao.update(updatedIndexWeight)
                }
                launch(Dispatchers.IO) {
                    syncService.cloudUpdater?.let { updater ->
                        val selectedStore = storeDao.getSelectedStore()
                        val shoppingList = shoppingListDao.getShoppingList()
                        val selectedStoreCloudId = selectedStore?.cloudId
                        val shoppingListCloudId = shoppingList?.cloudId
                        if (selectedStoreCloudId != null) {
                            debugLog(
                                "Updating IndexWeight in cloud: ${updatedIndexWeight.itemName} for store ${selectedStore.storeName}",
                                LOG_TAG
                            )
                            updater.saveIndexWeight(updatedIndexWeight, selectedStoreCloudId)
                        } else {
                            errorLog(
                                "No cloudId found for selected store ${selectedStore?.storeName}. Skipping update of ${item.itemName}.",
                                LOG_TAG
                            )
                        }
                        if (shoppingListCloudId != null) {
                            debugLog(
                                "Updating ShoppingListItem in cloud: ${updatedItem.itemName}",
                                LOG_TAG
                            )
                            updater.saveShoppingListItem(updatedItem, shoppingListCloudId)
                        } else {
                            errorLog(
                                "No cloudId found for shopping list. Skipping update of ${item.itemName}.",
                                LOG_TAG
                            )
                        }
                    }
                }

            } else {
                warningLog(
                    "Skipping update for IndexWeight with id 0 for item ${item.itemName}. This indicates an issue in data preparation.",
                    LOG_TAG
                )
            }
        }
    }

    suspend fun moveItem(
        from: Int,
        to: Int,
        currentItems: List<ShoppingItemWithWeight>
    ): List<ShoppingItemWithWeight> = coroutineScope {
        if (from == to || from < 0 || to < 0 || from >= currentItems.size || to >= currentItems.size) {
            return@coroutineScope currentItems // Bounds check
        }

        val itemMoved = currentItems[from]
        val itemReplaced = currentItems[to]

        // Ensure weights are valid before proceeding
        if (itemMoved.indexWeight.id == 0L || itemReplaced.indexWeight.id == 0L) {
            warningLog(
                "Attempted to move item with unpersisted IndexWeight. itemMoved: ${itemMoved.shoppingListItem.itemName}, itemReplaced: ${itemReplaced.shoppingListItem.itemName}",
                LOG_TAG
            )
            // Potentially re-fetch the list from DB to get fresh, persisted items, or throw an error.
            // For now, return currentItems to avoid crash, but this signals a deeper issue if it occurs.
            return@coroutineScope currentItems
        }

        val goingUp = itemMoved.indexWeight.weight < itemReplaced.indexWeight.weight
        val newIndexWeightValue = if (goingUp) {
            itemReplaced.indexWeight.weight + INDEX_INCREMENT
        } else {
            itemReplaced.indexWeight.weight - INDEX_INCREMENT
        }

        val updatedItems = currentItems
            .map { item ->
                if (item.shoppingListItem.id == itemMoved.shoppingListItem.id) {
                    item.copy(
                        indexWeight = item.indexWeight.copy(
                            weight = newIndexWeightValue,
                            updatedAt = getCurrentTime()
                        )
                    )
                } else {
                    item
                }
            }.sortedByDescending { it.indexWeight.weight }

        if (updatedItems.groupBy { it.indexWeight.weight }.any { it.value.size >= 2 }) {
            reIndexWeights(updatedItems)
        } else {
            updatedItems
        }
    }

    private fun reIndexWeights(
        items: List<ShoppingItemWithWeight>
    ): List<ShoppingItemWithWeight> {
        return items
            .sortedByDescending { it.indexWeight.weight } // Ensure sorted before re-indexing
            .mapIndexed { index, item ->
                val newWeight =
                    (items.size - index) * DEFAULT_INDEX_GAP // Adjusted to be 1-based effectively
                item.copy(
                    indexWeight = item.indexWeight.copy(
                        weight = newWeight,
                        updatedAt = getCurrentTime()
                    )
                )
            }
    }

    fun getSearchResults(query: String): Flow<List<SingleItem>> {
        return shoppingListItemDao.getSearchResults(query)
    }

    fun getSearchResults(query: String, exceptItems: List<String>): Flow<List<SingleItem>> {
        return shoppingListItemDao.getSearchResultsExcept(query, exceptItems)
    }

    suspend fun deleteItem(itemWithIndex: ShoppingItemWithWeight) = coroutineScope {
        // make sure it is latest version of SHoppingListItem to get the cloudId
        val shoppingListItem =
            withContext(Dispatchers.Default) { shoppingListItemDao.getShoppingListItem(itemWithIndex.itemId) }
        launch(Dispatchers.Default) {
            if (itemWithIndex.indexWeight.id != 0L) {
                // Only delete if it's a persisted weight
                // Also delete the associated IndexWeight
                indexWeightDao.delete(itemWithIndex.indexWeight)
            }
            shoppingListItemDao.deleteById(itemWithIndex.itemId)
        }
        launch(Dispatchers.IO) {
            val updater = syncService.cloudUpdater
            val shoppingListItemCloudId = shoppingListItem?.cloudId
            val indexWeightCloudId = itemWithIndex.indexWeight
            if (updater != null && shoppingListItemCloudId != null) {
                updater.deleteShoppingListItem(shoppingListItem)
            }
        }
    }

    fun getSuggestedShoppingListItems(query: String): Flow<List<SuggestedItem>> {
        return combine(
            shoppingListItemDao.getSearchResults(query),
            recipesDao.getRecipesWithItemsFlow(query)
        ) { searchResults, recipes ->
            val searchResultsSuggestedItems =
                searchResults.map { SuggestedItem(it.itemName, SuggestedItemType.ITEM) }
            val recipesSuggestedItems =
                recipes.map { SuggestedItem(it.recipe.name, SuggestedItemType.RECIPE) }
            val combinedList = searchResultsSuggestedItems + recipesSuggestedItems
            combinedList.sortedBy { it.itemName }
        }
    }

    fun getSuggestedRecipeIngredients(query: String): Flow<List<SuggestedItem>> {
        return combine(
            shoppingListItemDao.getSearchResults(query),
            recipesDao.getRecipesWithItemsFlow(query) // This seems to be getting full recipes, maybe just items?
        ) { searchResults, _ -> // recipes might not be needed if only suggesting single items
            val searchResultsSuggestedItems =
                searchResults.map { SuggestedItem(it.itemName, SuggestedItemType.ITEM) }
            searchResultsSuggestedItems.sortedBy { it.itemName }
        }
    }

    suspend fun deleteSuggestedItem(item: SuggestedItem) {
        when (item.type) {
            SuggestedItemType.ITEM -> singleItemDao.deleteSingleItem(SingleItem(item.itemName))
            SuggestedItemType.RECIPE -> recipesDao.getRecipeByName(item.itemName)?.let { recipe ->
                recipesDao.deleteRecipe(recipe)
            }
        }
    }
}
