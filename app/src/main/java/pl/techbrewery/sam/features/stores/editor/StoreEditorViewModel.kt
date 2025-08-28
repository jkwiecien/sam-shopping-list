package pl.techbrewery.sam.features.stores.editor

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.shared.BaseViewModel

class StoreEditorViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    private var storeId: Long = -1
    var storeName: String by mutableStateOf("")
        private set
    var storeAddress: String by mutableStateOf("")
        private set

    fun setStoreId(existingStoreId: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                storeRepository.getStore(existingStoreId)
            }?.let { store ->
                storeId = store.storeId
                storeName = store.storeName
            }
        }
    }

    fun clearState() {
        storeId = -1
        storeName = ""
        storeAddress = ""
    }

    override fun onAction(action: Any) {
        when (action) {
            is SaveStorePressed -> saveStore()
            is StoreNameChanged -> storeName = action.name
            is StoreAddressChanged -> storeAddress = action.name
        }
    }

    private fun saveStore() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                storeRepository.saveStore(
                    storeId = storeId,
                    storeName = storeName,
                    storeAddress = storeAddress,
                )
                emitSingleAction(StoreUpdated)
            }
        }
    }
}