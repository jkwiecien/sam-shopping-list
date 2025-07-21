package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.features.navigation.NavigationViewModel
import pl.techbrewery.sam.features.shoppinglist.state.ShoppingListItemsState
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.BottomPageContentState
import pl.techbrewery.sam.shared.KeyboardDonePressed
import pl.techbrewery.sam.shared.SearchQueryChanged

class ShoppingListViewModel(
    private val navigation: NavigationViewModel,
    private val shoppingList: ShoppingListRepository,
    private val stores: StoreRepository
) : BaseViewModel() {

    private val mutableSearchFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFLow: StateFlow<String> = mutableSearchFlow

    var bottomSheetContentState: BottomPageContentState? by mutableStateOf(null)
        private set

    internal val itemsState: StateFlow<ShoppingListItemsState> =
        shoppingList.getLastShoppingList()
            .map { items ->
                ShoppingListItemsState(
                    items = items.toImmutableList()
                )
            }
            .stateIn(
                scope = viewModelScope,
                // Determines when the upstream flow is active and producing values.
                // SharingStarted.WhileSubscribed(5000) means the flow will stay active
                // for 5 seconds after the last subscriber disappears.
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ShoppingListItemsState()
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
            is KeyboardDonePressed -> onDonePressed()
            is SearchQueryChanged -> onSearchQueryChanged(action.query)
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

    private fun onDonePressed() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { shoppingList.insertItem(mutableSearchFlow.value) }
            clearSearchField()
        }
    }

    fun dismissBottomSheet() {
        bottomSheetContentState = null
    }
}