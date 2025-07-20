//package pl.techbrewery.sam.kmp.database.dao
//
//import pl.techbrewery.sam.kmp.database.entity.ItemBundle
//import pl.techbrewery.sam.kmp.database.entity.ItemBundleCrossRef
//import pl.techbrewery.sam.kmp.database.entity.SingleItem
//import pl.techbrewery.sam.kmp.database.pojo.SingleItemsBundle
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import androidx.room.Transaction
//import kotlinx.coroutines.flow.Flow // Import Flow
//
//@Dao
//interface ItemBundleDao {
//
//    // --- Insert Operations ---
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertItemBundle(itemBundle: ItemBundle): Long
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertSingleItem(singleItem: SingleItem): Long
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE) // Use IGNORE if a cross-ref might already exist
//    suspend fun insertBundleItemCrossRef(crossRef: ItemBundleCrossRef)
//
//    // --- Query Operations (Flows) ---
//
//    // Get all item bundles with their single items
//    @Transaction
//    @Query("SELECT * FROM item_bundles")
//    fun getItemBundlesWithSingleItems(): Flow<List<SingleItemsBundle>>
//
//    // Get a specific item bundle with its single items
//    // Returns Flow<ItemBundleWithSingleItems?> because the bundle might not exist
//    @Transaction
//    @Query("SELECT * FROM item_bundles WHERE bundleId = :bundleId")
//    fun getItemBundleWithSingleItems(bundleId: Long): Flow<SingleItemsBundle?>
//
//    // Get single items for a specific bundle (if you only need items, not the bundle object itself)
//    @Query(
//        """
//        SELECT si.* FROM single_items si
//        INNER JOIN item_bundle_cross_ref bicr ON si.item_name = bicr.itemName
//        WHERE bicr.bundleId = :bundleId
//    """
//    )
//    fun getSingleItemsForBundle(bundleId: Long): Flow<List<SingleItem>>
//
//    // Get a single item by ID
//    @Query("SELECT * FROM single_items WHERE item_name = :itemName")
//    fun getSingleItemByName(itemName: String): Flow<SingleItem?>
//
//    // --- Delete Operations ---
//    @Delete
//    suspend fun deleteItemBundle(itemBundle: ItemBundle)
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
//}