package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.store_default_name
import pl.techbrewery.sam.resources.store_main_default_name

class StoreRepository(
    private val kmpDatabase: KmpDatabase
) {
    suspend fun hasAnyStores(): Boolean {
        return kmpDatabase.storeDao().hasAnyStores()
    }

    private suspend fun validateStore(
        store: Store
    ): Store {
        val fallbackNameResource =
            if (store.main) Res.string.store_main_default_name else Res.string.store_default_name
        return if (store.name.isBlank()) store.copy(name = getString(fallbackNameResource))
        else store
    }

    suspend fun getStore(storeId: Long): Store? {
        return kmpDatabase.storeDao().getStoreById(storeId)?.let {
            validateStore(it)
        }
    }

    suspend fun getMainStore(): Store? {
        return kmpDatabase.storeDao().getMainStore()?.let {
            validateStore(it)
        }
    }

    suspend fun saveStoreLayout(
        storeId: Long,
        storeName: String,
        storeAddress: String? = null,
        departments: List<StoreDepartment>
    ) {
        var store = getStore(storeId)
        if (store != null) {
            kmpDatabase.storeDao().update(
                store.copy(
                    name = storeName,
                    address = storeAddress,
                    updatedAt = getCurrentTime()
                )
            )
        } else {
            store = Store(
                name = storeName,
                address = storeAddress
            )
            val newlyCreatedStoreId = kmpDatabase.storeDao().insert(
                Store(
                    name = storeName,
                    address = storeAddress
                )
            )
            store = store.copy(storeId = newlyCreatedStoreId)
            kmpDatabase.storeDao().insert(store)
        }


        departments.forEachIndexed { index, department ->
            val existingDepartment = kmpDatabase.storeDepartmentDao()
                .getStoreDepartment(store.storeId, department.departmentName)
            if (existingDepartment != null) {
                kmpDatabase.storeDepartmentDao().update(
                    existingDepartment.copy(
                        position = index
                    )
                )
            } else {
                kmpDatabase.storeDepartmentDao().insert(
                    department.copy(
                        storeId = store.storeId,
                        position = index
                    )
                )
            }
        }
        kmpDatabase.storeDao().insert(store)
    }


    private suspend fun createMainStoreIfNotPresent() {
        val mainStore = getMainStore()
        if (mainStore == null) {
            kmpDatabase.storeDao().insert(
                Store.createDefaultMainStore()
            )
        }
    }

    fun getAllStoresFlow(): Flow<List<Store>> {
        return kmpDatabase.storeDao().getAllStores()
            .onStart {
                createMainStoreIfNotPresent()
            }
            .map { stores ->
                stores.map { store -> validateStore(store) }
            }
    }

    suspend fun getStoreDepartments(storeId: Long): List<StoreDepartment> {
        return kmpDatabase.storeDepartmentDao().getStoreDepartments(storeId)
    }

    fun reindexDepartments(departments: List<StoreDepartment>): List<StoreDepartment> {
        return departments.mapIndexed { index, department ->
            department.copy(position = index)
        }
    }
}