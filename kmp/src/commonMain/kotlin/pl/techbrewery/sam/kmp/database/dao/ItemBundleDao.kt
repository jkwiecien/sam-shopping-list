package pl.techbrewery.sam.kmp.database.dao

import pl.techbrewery.sam.kmp.database.entity.ItemBundle
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // Import Flow
import pl.techbrewery.sam.kmp.database.entity.ItemBundleJoin
import pl.techbrewery.sam.kmp.database.pojo.ItemBundleWithItems

@Dao
interface ItemBundleDao {

    // --- Insert Operations ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItemBundle(itemBundle: ItemBundle): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRecipe(itemBundle: ItemBundle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleItem(singleItem: SingleItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredient(ingredient: ItemBundleJoin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredients(ingredients: List<ItemBundleJoin>)

    // --- Query Operations (Flows) ---

    // Get all item bundles with their single items
    @Transaction
    @Query("SELECT * FROM item_bundles")
    fun getItemBundlesWithSingleItems(): Flow<List<ItemBundleWithItems>>

    @Query("SELECT * FROM item_bundles WHERE bundle_id = :recipeId")
    suspend fun getRecipe(recipeId: Long): ItemBundle?

    @Query("SELECT * FROM item_bundles WHERE name = :recipName")
    suspend fun getRecipeByName(recipName: String): ItemBundle?

    // Get a specific item bundle with its single items
    // Returns Flow<ItemBundleWithSingleItems?> because the bundle might not exist
    @Transaction
    @Query("SELECT * FROM item_bundles WHERE bundle_id = :bundleId")
    fun getItemBundleWithSingleItemsFlow(bundleId: Long): Flow<ItemBundleWithItems?>

    @Transaction
    @Query("SELECT * FROM item_bundles WHERE bundle_id = :bundleId")
    suspend fun getItemBundleWithSingleItems(bundleId: Long): ItemBundleWithItems?

    // Get single items for a specific bundle (if you only need items, not the bundle object itself)
    @Query(
        """
        SELECT si.* FROM single_items si
        INNER JOIN item_bundle_join bicr ON si.item_name = bicr.item_name_join
        WHERE bicr.bundle_id_join = :bundleId
    """
    )
    fun getSingleItemsForBundle(bundleId: Long): Flow<List<SingleItem>>

    @Query("SELECT * FROM item_bundle_join WHERE bundle_id_join = :recipeId")
    suspend fun getIngredients(recipeId: Long): List<ItemBundleJoin>

//    // Get a single item by ID
//    @Query("SELECT * FROM single_items WHERE item_name = :itemName")
//    fun getSingleItemByName(itemName: String): Flow<SingleItem?>
//
    // --- Delete Operations ---
    @Delete
    suspend fun deleteRecipe(itemBundle: ItemBundle)

    @Delete
    suspend fun deleteRecipeIngredients(ingredients: List<ItemBundleJoin>)
//
//    @Delete
//    suspend fun deleteSingleItem(singleItem: SingleItem)
//
//    @Delete
//    suspend fun deleteBundleItemCrossRef(crossRef: ItemBundleCrossRef)
//
//    // A convenience method to remove a single item from a specific bundle
//    @Query("DELETE FROM item_bundle_cross_ref WHERE bundleId = :bundleId AND itemName = :itemName")
//    suspend fun deleteItemFromBundle(bundleId: Long, itemName: String)
//
//    // --- Update Operations (Example) ---
//    // @Update // Add this if you need to update entities
//    // suspend fun updateItemBundle(itemBundle: ItemBundle)
}