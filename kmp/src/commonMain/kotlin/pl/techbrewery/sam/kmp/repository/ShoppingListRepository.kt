package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.kmp.model.SuggestedItemType
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_item_already_in_shopping_list
import java.sql.SQLIntegrityConstraintViolationException

class ShoppingListRepository(
    private val db: KmpDatabase
) {
    private val singleItemDao get() = db.singleItemDao()
    private val storeDao get() = db.storeDao()
    private val shoppingListItemDao get() = db.shoppingListItemDao()
    private val recipesDao get() = db.recipeDao()

    suspend fun saveSearchResult(itemName: String) {
        singleItemDao.insertSingleItem(SingleItem(itemName = itemName))
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

    suspend fun addItemToShoppingList(itemName: String,  throwErrorOnDuplicate: Boolean = true) {
        val selectedStore = storeDao.getSelectedStore()!!
        val allSingleItems = singleItemDao.getAllSingleItems()
        val allShoppingItems = getShoppingListItemsForSelectedStore()
        if (allShoppingItems.any { allSingleItems.first { si -> si.itemName == it.itemName }.itemName.lowercase() == itemName.lowercase() }) {
            if (throwErrorOnDuplicate) throw SQLIntegrityConstraintViolationException(getString(Res.string.error_item_already_in_shopping_list))
            else return
        }
        val uncheckedItems = allShoppingItems.filterNot { item -> item.checkedOff }

        val maxWeight = uncheckedItems.maxOfOrNull { it.indexWeight } ?: 0L
        val newWeight = maxWeight + DEFAULT_INDEX_GAP


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
    fun getShoppingListItemsForSelectedStoreFlow(): Flow<List<ShoppingListItem>> {
        return storeDao.getSelectedStoreFlow()
            .filterNotNull()
            .flatMapLatest { store ->
                shoppingListItemDao.getShoppingListItemsForStoreFlow(store.storeId)
            }
    }

    suspend fun getShoppingListItemsForSelectedStore(): List<ShoppingListItem> {
        return storeDao.getSelectedStore()?.let { selectedStore ->
            shoppingListItemDao.getShoppingListItemsForStore(selectedStore.storeId)
        } ?: emptyList()
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

    fun getSuggestedShoppingListItems(query: String): Flow<List<SuggestedItem>> {
        return combine(
            shoppingListItemDao.getSearchResults(query),
            recipesDao.getRecipesWithItemsFlow(query)
        ) { searchResults, recipes ->
            tempLog("recipes for query: ${recipes.joinToString { it.recipe.name }}")
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
