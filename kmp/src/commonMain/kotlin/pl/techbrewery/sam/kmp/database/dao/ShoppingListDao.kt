package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.ShoppingList

@Dao
interface ShoppingListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shoppingList: ShoppingList): Long

    @Update
    suspend fun update(shoppingList: ShoppingList)

    @Query("SELECT * FROM shopping_lists")
    fun getAllShoppingLists(): List<ShoppingList>

    @Query("SELECT * FROM shopping_lists")
    fun getAllShoppingListsFlow(): Flow<List<ShoppingList>>

    @Query("SELECT EXISTS(SELECT 1 FROM shopping_lists)")
    suspend fun hasShoppingList(): Boolean

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getShoppingList(id: Long): ShoppingList?

    @Query("SELECT * FROM shopping_lists WHERE cloud_id = :cloudId")
    suspend fun getShoppingListByCloudId(cloudId: String): ShoppingList?

    @Query("SELECT * FROM shopping_lists ORDER BY id ASC LIMIT 1")
    suspend fun getShoppingList(): ShoppingList?

    @Query("SELECT * FROM shopping_lists ORDER BY id ASC LIMIT 1")
    fun getSelectedListFlow(): Flow<ShoppingList?>

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shopping_lists")
    suspend fun deleteAll()
}
