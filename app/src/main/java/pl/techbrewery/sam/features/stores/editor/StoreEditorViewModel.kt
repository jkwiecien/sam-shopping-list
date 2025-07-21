package pl.techbrewery.sam.features.stores.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.techbrewery.sam.features.stores.SaveStoreLayoutPressed
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoreEditorViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {

//    internal val items: StateFlow<ImmutableList<StoreDepartment>> =
//        stores.getLastShoppingList()
//            .map { items ->
//                items.toImmutableList()
//            }
//            .stateIn(
//                scope = viewModelScope,
//                // Determines when the upstream flow is active and producing values.
//                // SharingStarted.WhileSubscribed(5000) means the flow will stay active
//                // for 5 seconds after the last subscriber disappears.
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = emptyList<SingleItem>().toImmutableList()
//            )

    override fun onAction(action: Any) {
        when (action) {
            is SaveStoreLayoutPressed -> saveStore()
        }
    }

    private fun saveStore() {
        viewModelScope.launch(Dispatchers.Main) {
        }
    }
}