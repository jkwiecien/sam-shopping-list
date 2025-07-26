package pl.techbrewery.sam.features.stores

import android.R.attr.action
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_message_last_store_delete_forbidden
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.ToastRequested
import java.sql.SQLIntegrityConstraintViolationException

class StoresViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    internal val stores: StateFlow<ImmutableList<Store>> =
        storeRepository.getAllStoresFlow()
            .map { items ->
                items.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<Store>().toImmutableList()
            )

    override fun onAction(action: Any) {
        when (action) {
            is StoreDismissed -> deleteStore(action.store)
        }
    }

    private fun deleteStore(store: Store) {
        viewModelScope.launch(Dispatchers.Default + CoroutineExceptionHandler { _, error ->
            emitSingleAction(ToastRequested(error.message ?: ""))
        }) {
            storeRepository.deleteStore(store)
        }
    }

}