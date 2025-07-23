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
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.shared.BaseViewModel

class StoreEditorViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    private val storeDepartmentsMutableFlow: MutableStateFlow<List<StoreDepartment>> =
        MutableStateFlow(emptyList())
    internal val departments: StateFlow<ImmutableList<StoreDepartment>> =
        storeDepartmentsMutableFlow
            .map { it.toImmutableList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<StoreDepartment>().toImmutableList()
            )

    private var storeId: Long = -1
    var storeName: String by mutableStateOf("")
        private set
    var newDepartmentName: String by mutableStateOf("")
        private set

    fun setStoreId(existingStoreId: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            launch {
                withContext(Dispatchers.Default) {
                    storeRepository.getStore(existingStoreId)
                }?.let { store ->
                    storeId = store.storeId
                    storeName = store.name
                }
            }
            launch {
                storeDepartmentsMutableFlow.value = withContext(Dispatchers.Default) {
                    storeRepository.getStoreDepartments(existingStoreId)
                }
            }
        }
    }

    override fun onAction(action: Any) {
        when (action) {
            is SaveStorePressed -> saveStore()
            is StoreNameChanged -> storeName = action.name
            is StoreDepartmentMoved -> moveDepartment(action.from, action.to)
            is DepartmentNameChanged -> newDepartmentName = action.departmentName
            is KeyboardDonePressedOnDepartmentName -> addDepartment()
        }
    }

    private fun saveStore() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                storeRepository.saveStoreLayout(
                    storeId = storeId,
                    storeName = storeName,
                    departments = departments.value
                )
            }
        }
    }

    private fun addDepartment() {
        val updatedDepartments = departments.value.toMutableList().apply {
            add(
                StoreDepartment(
                    departmentId = this.size.toLong(),
                    storeId = storeId,
                    departmentName = newDepartmentName,
                    position = size
                )
            )
        }
        storeDepartmentsMutableFlow.value = updatedDepartments
        newDepartmentName = ""
    }

    private fun moveDepartment(from: Int, to: Int) {
        if (from == to) return
        val updatedDepartments = departments.value.toMutableList()
        val element = updatedDepartments.removeAt(from)
        updatedDepartments.add(to, element)
        val reIndexedDepartments = storeRepository.reindexDepartments(updatedDepartments)
        tempLog(reIndexedDepartments.joinToString { "${it.departmentName}: ${it.position}" })
        storeDepartmentsMutableFlow.value =reIndexedDepartments
            .toImmutableList()
    }
}