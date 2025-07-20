package pl.techbrewery.sam.features.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository

class ShoppingListViewModel(
    private val repository: ShoppingListRepository
) : ViewModel() {
    val items: StateFlow<ShoppingListScreenState> =
        repository.getLastShoppingList()
            .map { items ->
                ShoppingListScreenState(
                    items = items
                )
            }
            .stateIn(
                scope = viewModelScope,
                // Determines when the upstream flow is active and producing values.
                // SharingStarted.WhileSubscribed(5000) means the flow will stay active
                // for 5 seconds after the last subscriber disappears.
                started = SharingStarted.WhileSubscribed(5000),
                // The initial value for the StateFlow before the upstream Flow emits its first value.
                initialValue = ShoppingListScreenState()
            )
}