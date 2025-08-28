package pl.techbrewery.sam.kmp.database

import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.dao.ShoppingListDao
import pl.techbrewery.sam.kmp.database.dao.StoreDao
import pl.techbrewery.sam.kmp.database.entity.ShoppingList
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.store_default_name

fun getRoomDatabase(builder: RoomDatabase.Builder<KmpDatabase>): KmpDatabase {
    val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database = builder
        // Add migrations here if you have them
        // .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigrationOnDowngrade() //fixme For development, handle migrations properly in production
        .setQueryCoroutineContext(Dispatchers.IO) // Set coroutine context for queries
        .build()

    databaseScope.launch {
        if (!hasNecessaryData(database)) {
            createInitialData(database)
        }
    }

    return database
}

suspend private fun hasNecessaryData(
    database: KmpDatabase
): Boolean {
    val hasStore = database.storeDao().hasAnyStores()
    val hasShoppingList = database.shoppingListDao().hasShoppingList()
    return hasStore && hasShoppingList
}

private suspend fun createInitialData(
    database: KmpDatabase
) {
    createInitialStore(database.storeDao())
    createInitialShoppingList(database.shoppingListDao())
}

private suspend fun createInitialStore(storeDao: StoreDao) {
    val store = Store(0, storeName = getString(Res.string.store_default_name), selected = true)
    storeDao.insert(store)
}

private suspend fun createInitialShoppingList(shoppingListDao: ShoppingListDao) {
    val shoppingList = ShoppingList(0, selected = true)
    shoppingListDao.insert(shoppingList)
}