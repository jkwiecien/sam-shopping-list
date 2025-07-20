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
    suspend fun insert(store: Store)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<Store>)

    @Update
    suspend fun update(store: Store)

    @Delete
    suspend fun delete(store: Store)

    @Query("SELECT * FROM Store WHERE uid = :storeId")
    suspend fun getStoreById(storeId: Int): Store?

    @Query("SELECT * FROM Store ORDER BY name ASC")
    fun getAllStores(): Flow<List<Store>> // Observe changes with Flow

    @Query("SELECT * FROM Store WHERE name LIKE :searchQuery || '%' ORDER BY name ASC")
    fun searchStoresByName(searchQuery: String): Flow<List<Store>>

    @Query("DELETE FROM Store")
    suspend fun clearAll()
}