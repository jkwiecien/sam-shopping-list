package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.OnItemTextFieldFocusChanged
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.shared.SuggestedItemDeletePressed
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.LastScrollDirection
import java.sql.SQLIntegrityConstraintViolationException


class ShoppingListViewModel(
    private val shoppingList: ShoppingListRepository,
    private val storesRepository: StoreRepository
) : BaseViewModel() {

    private val mutableSearchFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFlow: StateFlow<String> = mutableSearchFlow
    var itemTextFieldError: String? by mutableStateOf(null)
        private set

    private val itemTextFieldFocusedMutableFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val selectedStoreDropdownItemFlow: StateFlow<DropdownItem<Store>> =
        storesRepository.getSelectedStoreFlow()
            .map {
                DropdownItem(
                    item = it,
                    text = it.name,
                    extraText = it.address
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Store.createInitialStore()
                    .let { DropdownItem(it, it.name, extraText = it.address) }
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

    private val shoppingListLastScrollDirectionMutableFlow: MutableStateFlow<LastScrollDirection> =
        MutableStateFlow(LastScrollDirection.NONE)

    private val lockDropdownStoreVisibilityChangeMutableFlow: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val showStoresDropdownFlow = combine(
        shoppingListLastScrollDirectionMutableFlow,
        storeDropdownItems,
        lockDropdownStoreVisibilityChangeMutableFlow
    ) { lastScrollDirection, dropdownItems, isLocked ->
        val hasMultipleStores = dropdownItems.size > 1
        val visible = hasMultipleStores &&
                (lastScrollDirection == LastScrollDirection.NONE || lastScrollDirection == LastScrollDirection.DOWN)
        visible to isLocked
    }
        .distinctUntilChanged()
        .map { (visible, isLocked) ->
            visible && !isLocked
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    private var lockDropdownStoreVisibilityChangeJob: Job? = null
    private fun lockDropdownStoreVisibilityChange() {
        tempLog("Locking dropdown store visibility change")
        lockDropdownStoreVisibilityChangeJob?.cancel()
        lockDropdownStoreVisibilityChangeJob = viewModelScope.launch {
            lockDropdownStoreVisibilityChangeMutableFlow.value = true
            delay(500)
            lockDropdownStoreVisibilityChangeMutableFlow.value = false
            tempLog("Un-locking dropdown store visibility change")
        }
    }

    private val itemsMutableFlow: MutableStateFlow<List<ShoppingListItem>> =
        MutableStateFlow(emptyList())
    internal val itemsFlow: StateFlow<ImmutableList<ShoppingListItem>> =
        itemsMutableFlow
            .debounce { 50L }
            .map { items ->
                items.filterNot { it.checkedOff }.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<ShoppingListItem>().toImmutableList()
            )


    @OptIn(ExperimentalCoroutinesApi::class)
    internal val suggestedItemsDropdownItems: StateFlow<ImmutableList<DropdownItem<SuggestedItem>>> =
        combine(searchQueryFlow, itemTextFieldFocusedMutableFlow) { query, focused ->
            query to focused
        }
            .flatMapLatest { dataPair ->
                val query = dataPair.first
                val focused = dataPair.second
                if (query.isNotEmpty() && focused) {
                    shoppingList.getSuggestedShoppingListItems(query)
                } else {
                    flowOf(emptyList())
                }
            }
            .map { suggestedItems ->
                suggestedItems.map { item ->
                    DropdownItem(
                        item = item,
                        text = item.itemName
                    )
                }
            }
            .map { it.toImmutableList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList<DropdownItem<SuggestedItem>>().toImmutableList()
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
                shoppingList.getShoppingListItemsForSelectedStoreFlow().collect {
                    itemsMutableFlow.value = it
                }
            }
        }
    }

    override fun onAction(action: Any) {
        when (action) {
            is ItemChecked -> onItemChecked(action.itemId)
            is ItemFieldKeyboardDonePressed -> addItem()
            is SearchQueryChanged -> onSearchQueryChanged(action.query)
            is ItemMoved -> moveItemMutableFlow.value = action.from to action.to
            is StoreDropdownItemSelected -> saveSelectedStore(action.dropdownItem.item)
            is SuggestedItemSelected -> addSuggestedItem(action.item)
            is ShoppingListItemDismissed -> deleteItem(action.item)
            is SuggestedItemDeletePressed -> deleteSuggestedItem(action.item)
            is OnItemTextFieldFocusChanged ->      itemTextFieldFocusedMutableFlow.value = action.focused
        }
    }

    private fun deleteSuggestedItem(item: SuggestedItem) {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Default) { shoppingList.deleteSuggestedItem(item) }
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
        }
    }


    private fun onSearchQueryChanged(query: String) {
        mutableSearchFlow.value = query
        itemTextFieldError = null
    }

    private fun clearSearchField() {
        mutableSearchFlow.value = ""
    }

    private fun addSuggestedItem(item: SuggestedItem) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                shoppingList.addSuggestedItemToShoppingList(item)
            }
        }
        mutableSearchFlow.value = ""
    }

    private fun addItem() {
        viewModelScope.launch(Dispatchers.Main + CoroutineExceptionHandler { _, error ->
            when (error) {
                is SQLIntegrityConstraintViolationException -> itemTextFieldError = error.message
            }
            clearSearchField()
        }) {
            withContext(Dispatchers.Default) {
                shoppingList.addItemToShoppingList(
                    itemName = mutableSearchFlow.value
                )
            }
            clearSearchField()
        }
    }

    fun deleteItem(item: ShoppingListItem) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { shoppingList.deleteItem(item) }
        }
    }

    private fun moveItem(from: Int, to: Int) {
        moveItemMutableFlow.value = from to to
        viewModelScope.launch(Dispatchers.Default) {
            val updatedItems = shoppingList.moveItem(from, to, itemsFlow.value)
            itemsMutableFlow.value = updatedItems
            launch { withContext(Dispatchers.Default) { shoppingList.updateItems(updatedItems) } }
        }
    }

    fun onShoppingListScrolled(scrollDirection: LastScrollDirection) {
        shoppingListLastScrollDirectionMutableFlow.value = scrollDirection
    }

    fun onShoppingListBouncedOffBottom(atBottom: Boolean) {
        val lastScrollDirection = shoppingListLastScrollDirectionMutableFlow.value
        if (lastScrollDirection == LastScrollDirection.DOWN && atBottom) lockDropdownStoreVisibilityChange()
    }
}