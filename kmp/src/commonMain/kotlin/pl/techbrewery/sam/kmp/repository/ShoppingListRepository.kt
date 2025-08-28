package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.cloud.CloudRepository
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.model.ShoppingItemWithWeight
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.kmp.model.SuggestedItemType
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_item_already_in_shopping_list
import java.sql.SQLIntegrityConstraintViolationException

class ShoppingListRepository(
    private val db: KmpDatabase,
    private val cloud: CloudRepository
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
        cloud.cloudUpdater?.let { launch { it.saveSingleItem(singleItem) } }
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
            shoppingListItemDao.update(shoppingListItem.copy(checkedOff = false, updatedAt = getCurrentTime()))
        } else {
            shoppingListItem = ShoppingListItem(
                itemName = singleItem.itemName,
                listId = selectedList.id,
                checkedOff = false
            )
            shoppingListItemDao.insert(
                shoppingListItem
            )
            indexWeightDao.insert(
                IndexWeight(
                    itemName = shoppingListItem.itemName,
                    storeId = selectedStore.storeId,
                    weight = newWeight
                )
            )
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
                shoppingListItemDao.getShoppingListItemsFlow(list.id)
                    .map { items ->
                        val storeId = storeDao.getSelectedStore()!!.storeId
                        items.map { item ->
                            val indexWeight = indexWeightDao.getIndexWeight(item.itemName, storeId)
                                ?: IndexWeight(itemName = item.itemName, storeId = storeId)
                            ShoppingItemWithWeight(item, indexWeight)
                        }.sortedByDescending { it.indexWeight.weight }
                    }
            }
    }

    suspend fun getShoppingListItemsForSelectedStore(): List<ShoppingItemWithWeight> {
        val storeId = storeDao.getSelectedStore()?.storeId ?: return emptyList()
        return shoppingListDao.getShoppingList()?.let { selectedList ->
            shoppingListItemDao.getShoppingListItemsForList(selectedList.id).map { item ->
                val indexWeight = indexWeightDao.getIndexWeight(item.itemName, storeId)
                    ?: IndexWeight(itemName = item.itemName, storeId = storeId)
                ShoppingItemWithWeight(item, indexWeight)
            }.sortedByDescending { it.indexWeight.weight }
        } ?: emptyList()
    }

    fun getAllItemsFlow(): Flow<List<SingleItem>> {
        return singleItemDao.getAllSingleItemsFlow()
    }

    suspend fun updateItems(items: List<ShoppingItemWithWeight>) {
        items.forEach { (item, weight) ->
            shoppingListItemDao.update(item.copy(updatedAt = getCurrentTime()))
            indexWeightDao.update(weight.copy(updatedAt = getCurrentTime()))
        }
    }

    suspend fun moveItem(
        from: Int,
        to: Int,
        currentItems: List<ShoppingItemWithWeight>
    ): List<ShoppingItemWithWeight> =
        coroutineScope {
            if (from == to) return@coroutineScope currentItems

            val itemMoved = currentItems[from]
            val itemReplaced = currentItems[to]
            val goingUp = itemMoved.indexWeight.weight < itemReplaced.indexWeight.weight
            val newIndexWeight = if (goingUp) {
                itemReplaced.indexWeight.weight + INDEX_INCREMENT
            } else {
                itemReplaced.indexWeight.weight - INDEX_INCREMENT
            }

            val updatedItems = currentItems
                .map { item ->
                    if (item.shoppingListItem.id == itemMoved.shoppingListItem.id) {
                        item.copy(indexWeight = item.indexWeight.copy(weight = newIndexWeight, updatedAt = getCurrentTime()))
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
            .sortedByDescending { it.indexWeight.weight }
            .mapIndexed { index, item ->
                val newWeight = (items.size - index + 1) * DEFAULT_INDEX_GAP
                item.copy(indexWeight = item.indexWeight.copy(weight = newWeight, updatedAt = getCurrentTime()))
            }
    }

    fun getSearchResults(query: String): Flow<List<SingleItem>> {
        return shoppingListItemDao.getSearchResults(query)
    }

    fun getSearchResults(query: String, exceptItems: List<String>): Flow<List<SingleItem>> {
        return shoppingListItemDao.getSearchResultsExcept(query, exceptItems)
    }

    suspend fun deleteItem(item: ShoppingItemWithWeight) {
        shoppingListItemDao.deleteById(item.itemId)
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
            recipesDao.getRecipesWithItemsFlow(query)
        ) { searchResults, recipes ->
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
