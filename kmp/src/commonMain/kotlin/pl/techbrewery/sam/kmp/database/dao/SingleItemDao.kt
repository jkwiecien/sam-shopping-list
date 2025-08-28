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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSingleItem(singleItem: SingleItem)

    @Query("SELECT * FROM single_items ORDER BY item_name ASC")
    fun getAllSingleItemsFlow(): Flow<List<SingleItem>>

    @Query("SELECT * FROM single_items ORDER BY item_name ASC")
    suspend fun getAllSingleItems(): List<SingleItem>

    @Query("SELECT * FROM single_items WHERE item_name = :itemName")
    fun getSingleItemByName(itemName: String): SingleItem?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSingleItem(singleItem: SingleItem)

    @Delete
    suspend fun deleteSingleItem(singleItem: SingleItem)

    @Query("DELETE FROM single_items WHERE item_name = :itemName")
    suspend fun deleteSingleItemByName(itemName: String): Int
}
