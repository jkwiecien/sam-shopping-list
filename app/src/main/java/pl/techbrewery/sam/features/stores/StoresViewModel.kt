package pl.techbrewery.sam.features.stores

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoresViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    internal val stores: StateFlow<ImmutableList<Store>> =
        storeRepository.getAllStores()
            .map { items ->
                items.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<Store>().toImmutableList()
            )

}