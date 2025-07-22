package pl.techbrewery.sam.kmp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import pl.techbrewery.sam.kmp.database.dao.ItemBundleDao
import pl.techbrewery.sam.kmp.database.dao.SingleItemDao
import pl.techbrewery.sam.kmp.database.dao.StoreDao
import pl.techbrewery.sam.kmp.database.dao.StoreDepartmentDao
import pl.techbrewery.sam.kmp.database.entity.ItemBundle
import pl.techbrewery.sam.kmp.database.entity.ItemBundleJoin
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment

@Database(
    entities = [
        Store::class,
        SingleItem::class,
        ItemBundle::class,
        ItemBundleJoin::class,
        StoreDepartment::class],
    version = 1
)
abstract class KmpDatabase : RoomDatabase() {
    abstract fun singleItemDao(): SingleItemDao
    abstract fun itemBundleDao(): ItemBundleDao
    abstract fun storeDao(): StoreDao
    abstract fun storeDepartmentDao(): StoreDepartmentDao
}

// Room compiler generates the 'actual' implementations for this constructor
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object KmpDatabaseConstructor : RoomDatabaseConstructor<KmpDatabase> {
    override fun initialize(): KmpDatabase
}