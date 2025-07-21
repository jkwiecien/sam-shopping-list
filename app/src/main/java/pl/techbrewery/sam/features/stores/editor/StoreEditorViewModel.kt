package pl.techbrewery.sam.features.stores.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.techbrewery.sam.features.stores.SaveStoreLayoutPressed
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoreEditorViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {

    var screenTitle: String by mutableStateOf("Create store layout")
        private set

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