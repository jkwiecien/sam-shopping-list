package pl.techbrewery.sam.kmp.repository

import androidx.sqlite.throwSQLiteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull // Added import
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.dao.StoreDao
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_message_last_store_delete_forbidden
import pl.techbrewery.sam.resources.store_default_name
import java.sql.SQLIntegrityConstraintViolationException
import kotlin.io.path.Path

class StoreRepository(
    private val db: KmpDatabase
) {
    val storeDao: StoreDao get() = db.storeDao()
    suspend fun hasAnyStores(): Boolean {
        return storeDao.hasAnyStores()
    }

    private suspend fun validateStore(
        store: Store
    ): Store {
        return if (store.name.isBlank()) store.copy(name = getString(Res.string.store_default_name))
        else store
    }

    suspend fun getStore(storeId: Long): Store? {
        return storeDao.getStoreById(storeId)?.let {
            validateStore(it)
        }
    }

    suspend fun getSelectedStore(): Store? {
        return storeDao.getSelectedStore()?.let {
            validateStore(it)
        }
    }

    suspend fun saveStoreLayout(
        storeId: Long,
        storeName: String,
        storeAddress: String = "",
        departments: List<StoreDepartment>
    ) {
        var store = getStore(storeId)
        if (store != null) {
            storeDao.update(
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
            val newlyCreatedStoreId = storeDao.insert(
                Store(
                    name = storeName,
                    address = storeAddress
                )
            )
            store = store.copy(storeId = newlyCreatedStoreId)
            storeDao.insert(store)
        }


        departments.forEachIndexed { index, department ->
            val existingDepartment = db.storeDepartmentDao()
                .getStoreDepartment(store.storeId, department.departmentName)
            if (existingDepartment != null) {
                db.storeDepartmentDao().update(
                    existingDepartment.copy(
                        position = index
                    )
                )
            } else {
                db.storeDepartmentDao().insert(
                    department.copy(
                        storeId = store.storeId,
                        position = index
                    )
                )
            }
        }
    }


    private suspend fun createInitialStoreIfNotPresent() {
        val firstStore = getSelectedStore()
        if (firstStore == null) {
            storeDao.insert(
                validateStore(Store.createInitialStore())
            )
        }
    }

    fun getAllStoresFlow(): Flow<List<Store>> {
        return storeDao.getAllStoresFlow()
            .map { stores ->
                stores.map { store -> validateStore(store) }
            }
    }

    suspend fun getStoreDepartments(storeId: Long): List<StoreDepartment> {
        return db.storeDepartmentDao().getStoreDepartments(storeId)
    }

    fun reindexDepartments(departments: List<StoreDepartment>): List<StoreDepartment> {
        return departments.mapIndexed { index, department ->
            department.copy(position = index)
        }
    }

    fun getSelectedStoreFlow(): Flow<Store> {
        return storeDao.getSelectedStoreFlow() // Returns Flow<Store?>
            .onStart { createInitialStoreIfNotPresent() }
            .filterNotNull() // Ensures only non-null Stores are emitted
    }

    suspend fun saveSelectedStore(newSelectedStoreId: Long) {
        val currentSelectedStore = storeDao.getSelectedStore()!!.copy(selected = false)
        val storesToUpdate = mutableListOf(currentSelectedStore)
        storeDao.getStoreById(newSelectedStoreId)?.let { newSelectedStore ->
            storesToUpdate.add(newSelectedStore.copy(selected = true))
            updateStores(storesToUpdate)
        }
    }

    suspend fun updateStores(stores: List<Store>) {
        storeDao.update(stores)
    }

    suspend fun deleteStore(store: Store) {
        if (store.selected) {
            val otherStores = storeDao.getStoresOtherThan(store.storeId)
            if (otherStores.isNotEmpty()) {
                storeDao.delete(store)
                storeDao.update(otherStores.first().copy(selected = true))
            } else {
               throw SQLIntegrityConstraintViolationException(getString(Res.string.error_message_last_store_delete_forbidden))
            }
        } else {
            storeDao.delete(store)
        }
    }
}
