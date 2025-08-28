package pl.techbrewery.sam.kmp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import pl.techbrewery.sam.kmp.database.dao.IndexWeightDao
import pl.techbrewery.sam.kmp.database.dao.RecipeDao // Changed from ItemBundleDao
import pl.techbrewery.sam.kmp.database.dao.ShoppingListDao
import pl.techbrewery.sam.kmp.database.dao.ShoppingListItemDao
import pl.techbrewery.sam.kmp.database.dao.SingleItemDao
import pl.techbrewery.sam.kmp.database.dao.StoreDao
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.entity.ShoppingList
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store

@Database(
    entities = [
        Store::class,
        SingleItem::class,
        Recipe::class,
        RecipeItem::class,
        ShoppingListItem::class,
        ShoppingList::class,
        IndexWeight::class
    ],
    version = 1
)
abstract class KmpDatabase : RoomDatabase() {
    abstract fun singleItemDao(): SingleItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun storeDao(): StoreDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun indexWeightDao(): IndexWeightDao
}

// Room compiler generates the 'actual' implementations for this constructor
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object KmpDatabaseConstructor : RoomDatabaseConstructor<KmpDatabase> {
    override fun initialize(): KmpDatabase
}
