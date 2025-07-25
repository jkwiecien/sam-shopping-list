package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.SingleItem

@Dao
interface SingleItemDao {
    // --- Insert Operations ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSingleItem(singleItem: SingleItem): Long

    // --- Query Operations (Flows) ---

    // Get all single items
    @Query("SELECT * FROM single_items ORDER BY index_weight DESC")
    fun getAllSingleItemsFlow(): Flow<List<SingleItem>>

    @Query("SELECT * FROM single_items WHERE item_name LIKE '%' || :query || '%' AND checked_off = true ORDER BY item_name ASC")
    fun getSuggestedItemsFlow(query: String): Flow<List<SingleItem>>

    @Query("SELECT * FROM single_items ORDER BY index_weight DESC")
    suspend fun getAllSingleItems(): List<SingleItem>

    @Query("SELECT * FROM single_items WHERE store_id = :storeId ORDER BY index_weight DESC")
    suspend fun getAllSingleItemsForStore(storeId: Long): List<SingleItem>

    @Query("SELECT * FROM single_items WHERE checked_off = false ORDER BY index_weight DESC") // Order by name for consistency
    fun getUncheckedSingleItems(): Flow<List<SingleItem>>

    // Get a single item by its ID
    @Query("SELECT * FROM single_items WHERE item_id = :id")
    fun getSingleItemByIdAsFlow(id: Long): Flow<SingleItem?>

    @Query("SELECT * FROM single_items WHERE item_id = :id")
    fun getSingleItemById(id: Long): SingleItem?

   @Query("SELECT * FROM single_items WHERE item_name = :itemName AND store_id = :storeId")
   fun getSingleItemByName(itemName: String, storeId: Long) : SingleItem?

    // --- Update Operations ---
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSingleItem(singleItem: SingleItem)

    // --- Delete Operations ---
    @Delete
    suspend fun deleteSingleItem(singleItem: SingleItem)

    // Delete a single item by its ID
    @Query("DELETE FROM single_items WHERE item_id = :id")
    suspend fun deleteSingleItemById(id: Long): Int // Returns number of rows deleted

}