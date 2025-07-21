package pl.techbrewery.sam.features.stores.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoreEditorViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    var storeName: String by mutableStateOf("")
        private set


    fun setStoreId(storeId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                storeRepository.getStoreWithDepartments(storeId)
            }?.let { storeWithDepartments ->
               storeName = storeWithDepartments.store.name
            }
        }
    }

    override fun onAction(action: Any) {
        when (action) {
            is SaveStorePressed -> saveStore()
            is StoreNameChanged -> storeName = action.name
        }
    }

    private fun saveStore() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                storeRepository.saveStoreLayout(
                    storeName = storeName,
                    departments = emptyList()
                )
            }
        }
    }
}