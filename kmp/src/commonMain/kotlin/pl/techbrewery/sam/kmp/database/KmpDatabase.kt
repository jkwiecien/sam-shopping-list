package pl.techbrewery.sam.kmp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import pl.techbrewery.sam.kmp.database.dao.SingleItemDao
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store

@Database(
//    entities = [Store::class, SingleItem::class, ItemBundle::class, ItemBundleCrossRef::class],
    entities = [Store::class, SingleItem::class],
    version = 1
)
abstract class KmpDatabase : RoomDatabase() {
    abstract fun singleItemDao(): SingleItemDao
//    abstract fun itemBundleDao(): ItemBundleDao
}

// Room compiler generates the 'actual' implementations for this constructor
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object KmpDatabaseConstructor : RoomDatabaseConstructor<KmpDatabase> {
    override fun initialize(): KmpDatabase
}