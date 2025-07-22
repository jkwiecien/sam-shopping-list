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
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoreEditorViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    private val storeDepartmentsMutableFlow: MutableStateFlow<List<StoreDepartment>> =
        MutableStateFlow(emptyList())
    internal val departments: StateFlow<ImmutableList<StoreDepartment>> =
        storeDepartmentsMutableFlow
            .map { items ->
                items.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<StoreDepartment>().toImmutableList()
            )

    var storeName: String by mutableStateOf("")
        private set

    fun setStoreId(storeId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                storeRepository.getStoreWithDepartments(storeId)
            }?.let { storeWithDepartments ->
                storeName = storeWithDepartments.store.name
                storeDepartmentsMutableFlow.value = storeWithDepartments.departments
            }
        }
    }

    override fun onAction(action: Any) {
        when (action) {
            is SaveStorePressed -> saveStore()
            is StoreNameChanged -> storeName = action.name
            is StoreDepartmentMoved -> moveDepartment(action.from, action.to)
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

    private fun moveDepartment(from: Int, to: Int) {
        val currentDepartments = departments.value.toMutableList()
        if (from == to) return
        val element = currentDepartments.removeAt(from)
        currentDepartments.add(to, element)
        storeDepartmentsMutableFlow.value = currentDepartments.toImmutableList()
    }
}