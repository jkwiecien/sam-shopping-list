package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.Store

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(store: Store): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<Store>)

    @Update
    suspend fun update(store: Store)

    @Update
    suspend fun update(stores: List<Store>)

    @Delete
    suspend fun delete(store: Store)

    @Query("SELECT * FROM stores WHERE store_id = :storeId")
    suspend fun getStoreById(storeId: Long): Store?

    @Query("SELECT * FROM stores WHERE selected = true")
    suspend fun getSelectedStore(): Store?

    @Query("SELECT * FROM stores WHERE selected = true")
    fun getSelectedStoreFlow(): Flow<Store>

    @Query("SELECT * FROM stores")
    fun getAllStoresFlow(): Flow<List<Store>>

    @Query("SELECT * FROM stores WHERE store_id != :storeId ORDER BY name ASC")
    suspend fun getStoresOtherThan(storeId: Long): List<Store>

    @Query("SELECT * FROM stores WHERE name LIKE :searchQuery || '%' ORDER BY name ASC")
    fun searchStoresByName(searchQuery: String): Flow<List<Store>>

    @Query("DELETE FROM stores")
    suspend fun clearAll()

    @Query("SELECT EXISTS(SELECT 1 FROM stores LIMIT 1)")
    suspend fun hasAnyStores(): Boolean
}