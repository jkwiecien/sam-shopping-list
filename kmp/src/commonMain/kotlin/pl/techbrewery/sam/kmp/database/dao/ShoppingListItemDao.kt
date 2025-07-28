package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem

@Dao
interface ShoppingListItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shoppingListItem: ShoppingListItem): Long

    @Update
    suspend fun update(shoppingListItem: ShoppingListItem)

    @Query("SELECT * FROM shopping_list_items WHERE store_id = :storeId AND checked_off = false ORDER BY index_weight DESC")
    fun getShoppingListItemsForStore(storeId: Long): Flow<List<ShoppingListItem>>

    @Query("SELECT si.* FROM single_items si JOIN shopping_list_items sli ON si.item_name = sli.item_name WHERE sli.store_id = :storeId AND sli.checked_off = false ORDER BY sli.index_weight DESC")
    fun getShoppingListForStore(storeId: Long): Flow<List<SingleItem>>

    @Query("SELECT * FROM shopping_list_items WHERE store_id = :storeId AND item_name = :itemName")
    suspend fun getShoppingListItem(storeId: Long, itemName: String): ShoppingListItem?

    @Query("SELECT * FROM shopping_list_items WHERE id = :itemId")
    suspend fun getShoppingListItem(itemId: Long): ShoppingListItem?

    @Query("SELECT * FROM single_items WHERE  item_name LIKE '%' || :query || '%' ORDER BY item_name ASC")
    fun getSearchResults(query: String): Flow<List<SingleItem>>

    @Query("SELECT * FROM single_items WHERE item_name LIKE '%' || :query || '%' AND item_name NOT IN (:exceptItems) ORDER BY item_name ASC")
    fun getSearchResultsExcept(query: String, exceptItems: List<String>): Flow<List<SingleItem>>

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
