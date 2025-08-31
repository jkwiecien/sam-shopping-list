package pl.techbrewery.sam.kmp.cloud

import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.entity.ShoppingList
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store

class CloudUpdater(
    private val cloud: CloudRepository,
    private val cloudSync: CloudSyncService,
    private val user: FirebaseUser
) {
    private val userId: String get() = user.uid

    suspend fun saveSingleItem(singleItem: SingleItem): String =
        withContext(Dispatchers.IO) {
            cloud.saveSingleItem(singleItem)
        }

    suspend fun saveStore(store: Store): String =
        withContext(Dispatchers.IO) {
            cloud.saveStore(store)
        }

    suspend fun saveShoppingList(shoppingList: ShoppingList): String =
        withContext(Dispatchers.IO) {
            cloud.saveShoppingList(shoppingList)
        }

    suspend fun saveShoppingListItem(item: ShoppingListItem, listCloudId: String): String =
        withContext(Dispatchers.IO) {
            cloud.saveShoppingListItem(item, listCloudId)
        }

    suspend fun saveIndexWeight(item: IndexWeight, storeCloudId: String): String =
        withContext(Dispatchers.IO) {
            cloud.saveIndexWeight(item, storeCloudId)
        }

    suspend fun saveRecipe(recipe: Recipe): String =
        withContext(Dispatchers.IO) {
            cloud.saveRecipe(recipe)
        }

    suspend fun saveRecipeItem(item: RecipeItem, recipeCloudId: String): String =
        withContext(Dispatchers.IO) {
            cloud.saveRecipeItem(item, recipeCloudId)
        }

    suspend fun deleteStore(store: Store) =
        withContext(Dispatchers.IO) {
            cloud.deleteStore(store)
        }

    suspend fun deleteShoppingListItem(item: ShoppingListItem) =
        withContext(Dispatchers.IO) {
            cloud.deleteShoppingListItem(item)
        }

    suspend fun deleteRecipe(recipe: Recipe) =
        withContext(Dispatchers.IO) {
            cloud.deleteRecipe(recipe)
        }

    suspend fun deleteRecipeItem(item: RecipeItem) =
        withContext(Dispatchers.IO) {
            cloud.deleteRecipeItem(item)
        }
}
