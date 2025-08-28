package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.cloud.CloudRepository
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.dao.StoreDao
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_message_last_store_delete_forbidden
import pl.techbrewery.sam.resources.store_default_name
import java.sql.SQLIntegrityConstraintViolationException

class StoreRepository(
    private val db: KmpDatabase,
    private val cloud: CloudRepository
) {
    val storeDao: StoreDao get() = db.storeDao()
    suspend fun hasAnyStores(): Boolean {
        return storeDao.hasAnyStores()
    }

    suspend fun getStore(storeId: Long): Store? {
        return storeDao.getStoreById(storeId)
    }

    suspend fun getSelectedStore(): Store? {
        return storeDao.getSelectedStore()
    }

    suspend fun saveStore(
        storeId: Long,
        storeName: String,
        storeAddress: String = ""
    ) = coroutineScope {
        var store = getStore(storeId)
        if (store != null) {
            store = store.copy(
                storeName = storeName,
                address = storeAddress,
                updatedAt = getCurrentTime()
            )
            storeDao.update(store)
        } else {
            store = Store(
                storeName = storeName,
                address = storeAddress
            )
            val newlyCreatedStoreId = storeDao.insert(
                Store(
                    storeName = storeName,
                    address = storeAddress
                )
            )
            store = store.copy(storeId = newlyCreatedStoreId)
            storeDao.insert(store)
        }
        cloud.cloudUpdater?.let { launch { it.saveStore(store) } }
    }

    fun getAllStoresFlow(): Flow<List<Store>> {
        return storeDao.getAllStoresFlow()
    }

    fun getSelectedStoreFlow(): Flow<Store> {
        return storeDao
            .getSelectedStoreFlow()
            .filterNotNull() // Ensures only non-null Stores are emitted
    }

    suspend fun saveSelectedStore(newSelectedStoreId: Long) {
        val currentSelectedStore = storeDao.getSelectedStore()!!.copy(selected = false, updatedAt = getCurrentTime())
        val storesToUpdate = mutableListOf(currentSelectedStore)
        storeDao.getStoreById(newSelectedStoreId)?.let { newSelectedStore ->
            storesToUpdate.add(newSelectedStore.copy(selected = true, updatedAt = getCurrentTime()))
            updateStores(storesToUpdate)
        }
    }

    suspend fun updateStores(stores: List<Store>) {
        storeDao.update(stores.map { it.copy(updatedAt = getCurrentTime()) })
    }

    suspend fun deleteStore(store: Store) {
        if (store.selected) {
            val otherStores = storeDao.getStoresOtherThan(store.storeId)
            if (otherStores.isNotEmpty()) {
                storeDao.delete(store)
                storeDao.update(otherStores.first().copy(selected = true, updatedAt = getCurrentTime()))
            } else {
                throw SQLIntegrityConstraintViolationException(getString(Res.string.error_message_last_store_delete_forbidden))
            }
        } else {
            storeDao.delete(store)
        }
    }
}
