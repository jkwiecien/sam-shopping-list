package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
import pl.techbrewery.sam.shared.KeyboardDonePressed
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.DropdownItem


class ShoppingListViewModel(
    private val shoppingList: ShoppingListRepository,
    private val storesRepository: StoreRepository
) : BaseViewModel() {

    private val mutableSearchFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFLow: StateFlow<String> = mutableSearchFlow
    var bottomSheetContentState: BottomPageContentState? by mutableStateOf(null)
        private set

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
                }.also { allStores ->
                    if (allStores.isNotEmpty()) {
                        selectedStoreDropdownItem = allStores.first()
                    }
                }.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList<DropdownItem<Store>>().toImmutableList()
            )

    var selectedStoreDropdownItem: DropdownItem<Store> by mutableStateOf(
        DropdownItem.dummyItem(
            Store.createDefaultMainStore()
        )
    )
        private set

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
                // Determines when the upstream flow is active and producing values.
                // SharingStarted.WhileSubscribed(5000) means the flow will stay active
                // for 5 seconds after the last subscriber disappears.
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<SingleItem>().toImmutableList()
            )

    private val moveItemMutableFlow: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(-1 to -1)
    private val moveItemFlow = moveItemMutableFlow
        .debounce { 50L } //this way O'm limiting glitches from library event overflow and filter out irrelevant calls
        .filter { it.first != it.second && it.first >= 0 && it.second >= 0 }


    init {
        viewModelScope.launch(Dispatchers.Default) {
//            val hasAnyStores = withContext(Dispatchers.Default) { stores.hasAnyStores() }
//            if (!hasAnyStores) bottomSheetContentState = CreateStoreBottomSheetState
            launch {
                itemsMutableFlow.value =
                    withContext(Dispatchers.Default) { shoppingList.getAllItems() }
            }

            launch {
                moveItemFlow.collect { pair ->
                    moveItem(pair.first, pair.second)
                }
            }

            launch {
                // Ensure that when the list of stores is loaded,
                // the first item is selected if nothing has been selected yet.
                // This handles the initial state or when the selected item becomes invalid.
                storeDropdownItems.first { it.isNotEmpty() }
            }
        }


    }

    override fun onCleared() {
        super.onCleared()
    }

    override fun onAction(action: Any) {
        when (action) {
            is ItemChecked -> onItemChecked(action.itemName)
            is KeyboardDonePressed -> addItem()
            is SearchQueryChanged -> onSearchQueryChanged(action.query)
            is ItemMoved -> moveItemMutableFlow.value = action.from to action.to
            is StoreDropdownItemSelected -> selectedStoreDropdownItem = action.dropdownItem
        }
    }

    private fun onItemChecked(itemName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                shoppingList.checkOffItem(itemName)
            }
            withContext(Dispatchers.Default) {
                itemsMutableFlow.value = shoppingList.getAllItems()
            }
        }
    }


    private fun onSearchQueryChanged(query: String) {
        mutableSearchFlow.value = query
    }

    private fun clearSearchField() {
        mutableSearchFlow.value = ""
    }

    private fun addItem() {
        viewModelScope.launch(Dispatchers.Main) {
            val currentItems = items.value
            val maxWeight = currentItems.maxOfOrNull { it.indexWeight } ?: 0L
            val newWeight = maxWeight + DEFAULT_INDEX_GAP
            tempLog("Adding new item with weight: $newWeight")
            withContext(Dispatchers.Default) {
                shoppingList.insertItem(
                    mutableSearchFlow.value,
                    newWeight
                )
            }
            itemsMutableFlow.value = withContext(Dispatchers.Default) { shoppingList.getAllItems() }
            clearSearchField()
        }
    }

    fun dismissBottomSheet() {
        bottomSheetContentState = null
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