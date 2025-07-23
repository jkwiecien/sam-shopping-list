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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.extensions.tempLog
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.kmp.utils.SamConfig.DEFAULT_INDEX_GAP
import pl.techbrewery.sam.kmp.utils.SamConfig.INDEX_INCREMENT
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.BottomPageContentState
import pl.techbrewery.sam.shared.KeyboardDonePressed
import pl.techbrewery.sam.shared.SearchQueryChanged



class ShoppingListViewModel(
    private val shoppingList: ShoppingListRepository,
    private val stores: StoreRepository
) : BaseViewModel() {

    private val mutableSearchFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFLow: StateFlow<String> = mutableSearchFlow

    var bottomSheetContentState: BottomPageContentState? by mutableStateOf(null)
        private set

    internal val items: StateFlow<ImmutableList<SingleItem>> =
        shoppingList.getAllItems()
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

//    init {
//        viewModelScope.launch(Dispatchers.Default) {
//            val hasAnyStores = withContext(Dispatchers.Default) { stores.hasAnyStores() }
//            if (!hasAnyStores) bottomSheetContentState = CreateStoreBottomSheetState
//        }
//    }

    override fun onAction(action: Any) {
        when (action) {
            is ItemChecked -> onItemChecked(action.itemName)
            is KeyboardDonePressed -> addItem()
            is SearchQueryChanged -> onSearchQueryChanged(action.query)
            is ItemMoved -> moveItem(action.from, action.to)
        }
    }

    private fun onItemChecked(itemName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { shoppingList.checkOffItem(itemName) }
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
            clearSearchField()
        }
    }

    fun dismissBottomSheet() {
        bottomSheetContentState = null
    }

    private fun moveItem(from: Int, to: Int) {
        if (from == to) return

        viewModelScope.launch(Dispatchers.Default) {
            shoppingList.moveItem(from, to, items.value)
        }
    }


    //if true, there are no duplicates



}