package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.BottomPageContentState
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.DropdownItem


class ShoppingListViewModel(
    private val shoppingList: ShoppingListRepository,
    private val storesRepository: StoreRepository
) : BaseViewModel() {

    private val mutableSearchFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFlow: StateFlow<String> = mutableSearchFlow
    var itemTextFieldError: String? by mutableStateOf(null)
        private set

    val selectedStoreDropdownItemFlow: StateFlow<DropdownItem<Store>> =
        storesRepository.getSelectedStoreFlow()
            .map {
                tempLog("Selected store collected: $it")
                DropdownItem(
                    item = it,
                    text = it.name,
                    extraText = it.address
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Store.createInitialStore().let { DropdownItem(it, it.name, extraText = it.address) }
            )
    internal val storeDropdownItems: StateFlow<ImmutableList<DropdownItem<Store>>> =
        storesRepository.getAllStoresFlow()
            .debounce { 50L }
            .map { allStores ->
                allStores.map { store ->
                    DropdownItem(
                        item = store,
                        text = store.name,
                        extraText = store.address
                    )
                }.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList<DropdownItem<Store>>().toImmutableList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val suggestedItemsDropdownItems: StateFlow<ImmutableList<DropdownItem<SingleItem>>> =
        searchQueryFlow
            .combine(selectedStoreDropdownItemFlow) { query, store ->
                query to store.item
            }
            .flatMapLatest { dataPair ->
                val query = dataPair.first
                val store = dataPair.second
                shoppingList.getSuggestedItems(store.storeId, query)
                    .map { suggestedItems ->
                        tempLog("Collected suggested items for query '$query': ${suggestedItems.joinToString { it.itemName }}")
                        suggestedItems.map { item ->
                            DropdownItem(
                                item = item,
                                text = item.itemName
                            )
                        }
                    }
            }
            .map { it.toImmutableList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList<DropdownItem<SingleItem>>().toImmutableList()
            )

    private val itemsMutableFlow: MutableStateFlow<List<SingleItem>> =
        MutableStateFlow(emptyList())
    internal val items: StateFlow<ImmutableList<SingleItem>> =
        itemsMutableFlow
            .debounce { 50L }
            .map { items ->
                tempLog("Got items: ${items.joinToString { "${it.itemName}:${it.indexWeight}" }}")
                items.filterNot { it.checkedOff }.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<SingleItem>().toImmutableList()
            )

    private val moveItemMutableFlow: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(-1 to -1)
    private val moveItemFlow = moveItemMutableFlow
        .debounce { 50L } //this way limiting glitches from library event overflow and filter out irrelevant calls
        .filter { it.first != it.second && it.first >= 0 && it.second >= 0 }


    init {
        viewModelScope.launch(Dispatchers.Default) {
            launch {
                moveItemFlow.collect { pair ->
                    moveItem(pair.first, pair.second)
                }
            }

            launch {
                selectedStoreDropdownItemFlow.collect {
                    itemsMutableFlow.value = shoppingList.getItemsForSelectedStore()
                }
            }
        }


    }

    override fun onCleared() {
        super.onCleared()
    }

    override fun onAction(action: Any) {
        when (action) {
            is ItemChecked -> onItemChecked(action.itemId)
            is ItemFieldKeyboardDonePressed -> addItem()
            is SearchQueryChanged -> onSearchQueryChanged(action.query)
            is ItemMoved -> moveItemMutableFlow.value = action.from to action.to
            is StoreDropdownItemSelected -> saveSelectedStore(action.dropdownItem.item)
            is SuggestedItemSelected -> addSuggestedItem(action.item)
        }
    }

    private fun saveSelectedStore(store: Store) {
        viewModelScope.launch(Dispatchers.Default) {
            storesRepository.saveSelectedStore(store.storeId)
        }
    }

    private fun onItemChecked(itemId: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                shoppingList.checkOffItem(itemId)
            }
            withContext(Dispatchers.Default) {
                itemsMutableFlow.value = shoppingList.getAllItems()
            }
        }
    }


    private fun onSearchQueryChanged(query: String) {
        mutableSearchFlow.value = query
        itemTextFieldError = null
    }

    private fun clearSearchField() {
        mutableSearchFlow.value = ""
    }

    private fun addSuggestedItem(item: SingleItem) {
        val uncheckedItem = item.copy(checkedOff = false)
        val updatedItems =
            itemsMutableFlow.value.plus(uncheckedItem).sortedByDescending { it.indexWeight }
        itemsMutableFlow.value = updatedItems
        mutableSearchFlow.value = ""
    }

    private fun addItem() {
        viewModelScope.launch(Dispatchers.Main) {
            val newItemName = mutableSearchFlow.value
            val currentItems = items.value.filterNot { it.checkedOff }
            //dont add duplicates. todo show error on text field in case of duplicate
            if (currentItems.any { it.itemName.lowercase() == newItemName.lowercase() }) {
                itemTextFieldError = "Already on the list"
                return@launch
            }
            val maxWeight = currentItems.maxOfOrNull { it.indexWeight } ?: 0L
            val newWeight = maxWeight + DEFAULT_INDEX_GAP
            tempLog("Adding new item with weight: $newWeight")
            withContext(Dispatchers.Default) {
                shoppingList.addItemToShoppingList(
                    newItemName,
                    newWeight
                )
            }
            itemsMutableFlow.value = withContext(Dispatchers.Default) { shoppingList.getItemsForSelectedStore() }
            clearSearchField()
        }
    }

    private fun moveItem(from: Int, to: Int) {
        moveItemMutableFlow.value = from to to


        val currentItems = items.value
        val fromItem = currentItems[from]
        val toItem = currentItems[to]
        tempLog("Moving item from ${fromItem.itemName}:${fromItem.indexWeight}($from) to ${toItem.itemName}:${toItem.indexWeight}($to)")

        viewModelScope.launch(Dispatchers.Default) {
            val updatedItems = shoppingList.moveItem(from, to, items.value)
            itemsMutableFlow.value = shoppingList.moveItem(from, to, items.value)
            launch { withContext(Dispatchers.Default) { shoppingList.updateItems(updatedItems) } }
        }
    }
}