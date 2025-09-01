package pl.techbrewery.sam.kmp.cloud

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.firestore
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.entity.ShoppingList
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.utils.debugLog

private const val LOG_TAG = "CloudRepository"

class CloudRepository(
    private val localDb: KmpDatabase,
) {
    private val firestore = Firebase.firestore
    val user get() = Firebase.auth.currentUser

    private val storeDao get() = localDb.storeDao()
    private val shoppingListDao get() = localDb.shoppingListDao()
    private val singleItemDao get() = localDb.singleItemDao()
    private val shoppingListItemDao get() = localDb.shoppingListItemDao()
    private val indexWeightDao get() = localDb.indexWeightDao()
    private val recipeDao get() = localDb.recipeDao()

    private fun getUserId(): String? {
        return user?.uid
    }

    suspend fun getStores(): List<Store> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.STORES)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
            .map { CloudConverter.storeFromSnapshot(it) }
    }

    suspend fun getShoppingListSnapshot(): DocumentSnapshot? {
        val userId = getUserId() ?: return null
        return firestore.collection(FirestoreCollections.SHOPPING_LISTS)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
            .firstOrNull()
    }

    suspend fun getShoppingListsSnapshots(): List<DocumentSnapshot> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.SHOPPING_LISTS)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
    }

    suspend fun getSingleItems(): List<SingleItem> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.SINGLE_ITEMS)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
            .map { CloudConverter.singleItemFromSnapshot(it) }
    }

    suspend fun getShoppingListItems(shoppingListId: String): List<ShoppingListItem> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.SHOPPING_LISTS)
            .document(shoppingListId)
            .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
            .map { CloudConverter.shoppingListItemFromSnapshot(it) }
    }

    suspend fun deleteAllItemsForList(shoppingListId: String) {
        val items = getShoppingListItems(shoppingListId)
        for (item in items) {
            item.cloudId?.let { // Ensure cloudId is not null before attempting to delete
                debugLog("Deleting ShoppingListItem from Firestore: cloudId: $it, parentListCloudId: $shoppingListId", LOG_TAG)
                firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                    .document(shoppingListId)
                    .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                    .document(it)
                    .delete()
            }
        }
    }

    suspend fun getIndexWeightSnapshots(): List<DocumentSnapshot> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.INDEX_WEIGHTS)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
    }

    suspend fun getRecipeSnapshots(): List<DocumentSnapshot> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.RECIPES)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
    }

    suspend fun getRecipeItems(recipeId: String): List<RecipeItem> {
        val userId = getUserId() ?: return emptyList()
        return firestore.collection(FirestoreCollections.RECIPES)
            .document(recipeId)
            .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
            .where { FieldPath("owner_id").equalTo(userId) }
            .get()
            .documents
            .map { CloudConverter.recipeItemFromSnapshot(it) }
    }

    suspend fun deleteAllItemsForRecipe(recipeId: String) {
        val items = getRecipeItems(recipeId)
        for (item in items) {
            item.cloudId?.let {
                debugLog("Deleting RecipeItem from Firestore: cloudId: $it, parentRecipeCloudId: $recipeId", LOG_TAG)
                firestore.collection(FirestoreCollections.RECIPES)
                    .document(recipeId)
                    .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                    .document(it)
                    .delete()
            }
        }
    }

    suspend fun saveStore(store: Store): String {
        val userId = getUserId()!!
        val existingStoreDoc = store.cloudId?.let { getStoreSnapshot(it) }
        val newCloudId: String
        if (existingStoreDoc == null || !existingStoreDoc.exists) {
            debugLog("Adding new Store to Firestore: ${store.storeName}, localId: ${store.storeId}", LOG_TAG)
            newCloudId = firestore.collection(FirestoreCollections.STORES)
                .add(CloudConverter.toSnapshot(store, userId))
                .id
        } else {
            debugLog("Updating Store in Firestore: ${store.storeName}, cloudId: ${existingStoreDoc.id}", LOG_TAG)
            firestore.collection(FirestoreCollections.STORES)
                .document(existingStoreDoc.id)
                .set(CloudConverter.toSnapshot(store, userId))
            newCloudId = existingStoreDoc.id
        }
        if (store.cloudId != newCloudId) {
            debugLog("Updating local Store ${store.storeName} with newCloudId: $newCloudId", LOG_TAG)
            storeDao.update(store.copy(cloudId = newCloudId))
        }
        return newCloudId
    }

    suspend fun saveShoppingList(shoppingList: ShoppingList): String {
        val userId = getUserId()!!
        val existingShoppingListSnapshot = getShoppingListSnapshot()
        val newCloudId: String
        if (existingShoppingListSnapshot == null) {
            debugLog("Adding new ShoppingList to Firestore, localId: ${shoppingList.id}", LOG_TAG)
            newCloudId = firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .add(CloudConverter.toSnapshot(shoppingList, userId))
                .id
        } else {
            debugLog("Updating ShoppingList in Firestore, cloudId: ${existingShoppingListSnapshot.id}", LOG_TAG)
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(existingShoppingListSnapshot.id)
                .set(CloudConverter.toSnapshot(shoppingList, userId))
            newCloudId = existingShoppingListSnapshot.id
        }
        if (shoppingList.cloudId != newCloudId) {
            debugLog("Updating local ShoppingList ${shoppingList.id} with newCloudId: $newCloudId", LOG_TAG)
            shoppingListDao.update(shoppingList.copy(cloudId = newCloudId))
        }
        return newCloudId
    }

    suspend fun saveSingleItem(singleItem: SingleItem): String {
        val userId = getUserId()!!
        val existingItemSnapshot = singleItem.cloudId?.let { getSingleItemSnapshot(it) }
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            debugLog("Adding new SingleItem to Firestore: ${singleItem.itemName}", LOG_TAG)
            resolvedCloudId = firestore.collection(FirestoreCollections.SINGLE_ITEMS)
                .add(CloudConverter.toSnapshot(singleItem, userId))
                .id
        } else {
            debugLog("Updating SingleItem in Firestore: ${singleItem.itemName}, cloudId: ${existingItemSnapshot.id}", LOG_TAG)
            firestore.collection(FirestoreCollections.SINGLE_ITEMS)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(singleItem, userId))
            resolvedCloudId = existingItemSnapshot.id
        }
        if (singleItem.cloudId != resolvedCloudId) {
            debugLog("Updating local SingleItem ${singleItem.itemName} with resolvedCloudId: $resolvedCloudId", LOG_TAG)
            singleItemDao.updateSingleItem(singleItem.copy(cloudId = resolvedCloudId))
        }
        return resolvedCloudId
    }

    suspend fun saveShoppingListItem(item: ShoppingListItem, listCloudId: String): String {
        val userId = getUserId()!!
        val existingItemSnapshot =
            item.cloudId?.let { getShoppingListItemSnapshot(listCloudId, it) }
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            debugLog("Adding new ShoppingListItem to Firestore: ${item.itemName}, localId: ${item.id}, listCloudId: $listCloudId", LOG_TAG)
            resolvedCloudId = firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(listCloudId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .add(CloudConverter.toSnapshot(item, userId, listCloudId))
                .id
        } else {
            debugLog("Updating ShoppingListItem in Firestore: ${item.itemName}, cloudId: ${existingItemSnapshot.id}, listCloudId: $listCloudId", LOG_TAG)
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(listCloudId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(item, userId, listCloudId))
            resolvedCloudId = existingItemSnapshot.id
        }
        if (item.cloudId != resolvedCloudId) {
            debugLog("Updating local ShoppingListItem ${item.itemName} with resolvedCloudId: $resolvedCloudId", LOG_TAG)
            shoppingListItemDao.update(item.copy(cloudId = resolvedCloudId))
        }
        return resolvedCloudId
    }

    suspend fun saveIndexWeight(item: IndexWeight, storeCloudId: String, shoppingListItemCloudId: String): String {
        val userId = getUserId()!!
        val shoppingListItem = shoppingListItemDao.getShoppingListItem(item.shoppingListItemId)
        val existingItemSnapshot = item.cloudId?.let { getIndexWeightSnapshot(it) }
        if (shoppingListItem == null) throw Exception("ShoppingListItem not found for IndexWeight (shoppingListItemId: ${item.shoppingListItemId})")
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            debugLog("Adding new IndexWeight to Firestore for item: ${shoppingListItem.itemName}, localId: ${item.id}, storeCloudId: $storeCloudId", LOG_TAG)
            resolvedCloudId = firestore.collection(FirestoreCollections.INDEX_WEIGHTS)
                .add(CloudConverter.toSnapshot(item, userId, storeCloudId, shoppingListItemCloudId))
                .id
        } else {
            debugLog("Updating IndexWeight in Firestore for item: ${shoppingListItem.itemName}, cloudId: ${existingItemSnapshot.id}, storeCloudId: $storeCloudId", LOG_TAG)
            firestore.collection(FirestoreCollections.INDEX_WEIGHTS)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(item, userId, storeCloudId, shoppingListItemCloudId))
            resolvedCloudId = existingItemSnapshot.id
        }
        if (item.cloudId != resolvedCloudId) {
            debugLog("Updating local IndexWeight for item ${shoppingListItem.itemName} with resolvedCloudId: $resolvedCloudId", LOG_TAG)
            indexWeightDao.update(item.copy(cloudId = resolvedCloudId))
        }
        return resolvedCloudId
    }

    suspend fun saveRecipe(recipe: Recipe): String {
        val userId = getUserId()!!
        val existingRecipeDoc = recipe.cloudId?.let { getRecipeSnapshot(it) }
        val newCloudId: String
        if (existingRecipeDoc == null || !existingRecipeDoc.exists) {
            debugLog("Adding new Recipe to Firestore: ${recipe.name}, localId: ${recipe.recipeId}", LOG_TAG)
            newCloudId = firestore.collection(FirestoreCollections.RECIPES)
                .add(CloudConverter.toSnapshot(recipe, userId))
                .id
        } else {
            debugLog("Updating Recipe in Firestore: ${recipe.name}, cloudId: ${existingRecipeDoc.id}", LOG_TAG)
            firestore.collection(FirestoreCollections.RECIPES)
                .document(existingRecipeDoc.id)
                .set(CloudConverter.toSnapshot(recipe, userId))
            newCloudId = existingRecipeDoc.id
        }
        if (recipe.cloudId != newCloudId) {
            debugLog("Updating local Recipe ${recipe.name} with newCloudId: $newCloudId", LOG_TAG)
            recipeDao.updateRecipe(recipe.copy(cloudId = newCloudId))
        }
        return newCloudId
    }

    suspend fun saveRecipeItem(item: RecipeItem, recipeCloudId: String): String {
        val userId = getUserId()!!
        val existingItemSnapshot = item.cloudId?.let { getRecipeItemSnapshot(recipeCloudId, it) }
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            debugLog("Adding new RecipeItem to Firestore: ${item.itemName}, localRecipeId: ${item.recipeId}, recipeCloudId: $recipeCloudId", LOG_TAG)
            resolvedCloudId = firestore.collection(FirestoreCollections.RECIPES)
                .document(recipeCloudId)
                .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                .add(CloudConverter.toSnapshot(item, userId, recipeCloudId))
                .id
        } else {
            debugLog("Updating RecipeItem in Firestore: ${item.itemName}, cloudId: ${existingItemSnapshot.id}, recipeCloudId: $recipeCloudId", LOG_TAG)
            firestore.collection(FirestoreCollections.RECIPES)
                .document(recipeCloudId)
                .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(item, userId, recipeCloudId))
            resolvedCloudId = existingItemSnapshot.id
        }
        if (item.cloudId != resolvedCloudId) {
            debugLog("Updating local RecipeItem ${item.itemName} with resolvedCloudId: $resolvedCloudId", LOG_TAG)
            recipeDao.updateRecipeItem(item.copy(cloudId = resolvedCloudId))
        }
        return resolvedCloudId
    }

    suspend fun deleteStore(store: Store) {
        store.cloudId?.let {
            debugLog("Deleting Store from Firestore: ${store.storeName}, cloudId: $it", LOG_TAG)
            firestore.collection(FirestoreCollections.STORES).document(it).delete()
        }
    }

    suspend fun deleteShoppingListItem(item: ShoppingListItem) {
        item.cloudId?.let {
            val listCloudId = shoppingListDao.getShoppingList(item.listId)?.cloudId ?: return
            debugLog("Deleting ShoppingListItem from Firestore: ${item.itemName}, cloudId: $it, parentListCloudId: $listCloudId", LOG_TAG)
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(listCloudId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .document(it)
                .delete()

//            val indexWeightQuerySnapshot = firestore.collection(FirestoreCollections.INDEX_WEIGHTS)
//                .where { FieldPath("owner_id").equalTo(userId) }
//                .where { FieldPath("item_name").equalTo(itemName) }
//                .get()
        }
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipe.cloudId?.let {
            debugLog("Deleting Recipe from Firestore: ${recipe.name}, cloudId: $it", LOG_TAG)
            deleteAllItemsForRecipe(it) // This will log its own deletions
            firestore.collection(FirestoreCollections.RECIPES).document(it).delete()
        }
    }

    suspend fun deleteRecipeItem(item: RecipeItem) {
        item.cloudId?.let {
            val recipeCloudId = recipeDao.getRecipe(item.recipeId)?.cloudId ?: return
            debugLog("Deleting RecipeItem from Firestore: ${item.itemName}, cloudId: $it, parentRecipeCloudId: $recipeCloudId", LOG_TAG)
            firestore.collection(FirestoreCollections.RECIPES)
                .document(recipeCloudId)
                .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                .document(it)
                .delete()
        }
    }

    // --- Snapshot Getters ---
    suspend fun getStoreSnapshot(cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.STORES).document(cloudId).get()

    suspend fun getShoppingListSnapshot(cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.SHOPPING_LISTS).document(cloudId).get()

    suspend fun getSingleItemSnapshot(cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.SINGLE_ITEMS).document(cloudId).get()

    suspend fun getShoppingListItemSnapshot(
        listCloudId: String,
        cloudId: String
    ): DocumentSnapshot =
        firestore.collection(FirestoreCollections.SHOPPING_LISTS)
            .document(listCloudId)
            .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
            .document(cloudId)
            .get()

    suspend fun getIndexWeightSnapshot(cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.INDEX_WEIGHTS).document(cloudId).get()

    suspend fun getRecipeSnapshot(cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.RECIPES).document(cloudId).get()

    suspend fun getRecipeItemSnapshot(recipeCloudId: String, cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.RECIPES)
            .document(recipeCloudId)
            .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
            .document(cloudId)
            .get()
}
