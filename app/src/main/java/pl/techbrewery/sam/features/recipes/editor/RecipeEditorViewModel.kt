package pl.techbrewery.sam.features.recipes.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.repository.RecipeRepository
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.ui.shared.DropdownItem

class RecipeEditorViewModel(
    private val repository: RecipeRepository,
    private val shoppingList: ShoppingListRepository
) : BaseViewModel() {
    private val searchQueryMutableFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFlow: StateFlow<String> = searchQueryMutableFlow

    var itemTextFieldError: String? by mutableStateOf(null)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val suggestedItemsDropdownItems: StateFlow<ImmutableList<DropdownItem<SingleItem>>> =
        searchQueryFlow
            .flatMapLatest { query ->
                shoppingList.getSuggestedItems(query)
                    .map { suggestedItems ->
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

    private val itemsMutableFlow: MutableStateFlow<List<SingleItem>> = MutableStateFlow(emptyList())
    internal val itemsFlow: StateFlow<ImmutableList<SingleItem>> =
        itemsMutableFlow
            .map { items ->
                items.filterNot { it.checkedOff }.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<SingleItem>().toImmutableList()
            )
}