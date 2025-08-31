package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pl.techbrewery.sam.kmp.database.entity.IndexWeight

@Dao
interface IndexWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(indexWeight: IndexWeight): Long

    @Update
    suspend fun update(indexWeight: IndexWeight)

    @Delete
    suspend fun delete(indexWeight: IndexWeight)

    @Query("SELECT * FROM index_weights WHERE item_name = :itemName AND store_id = :storeId")
    suspend fun getIndexWeight(itemName: String, storeId: Long): IndexWeight?

    @Query("SELECT * FROM index_weights")
    suspend fun getAll(): List<IndexWeight> // Added this method
}
