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

const val CLOUD_LOG_TAG = "Cloud"

class CloudRepository(
    private val localDb: KmpDatabase,
) {
    private val firestore = Firebase.firestore
    private val user get() = Firebase.auth.currentUser

    private val storeDao get() = localDb.storeDao()
    private val shoppingListDao get() = localDb.shoppingListDao()
    private val singleItemDao get() = localDb.singleItemDao()
    private val shoppingListItemDao get() = localDb.shoppingListItemDao()
    private val indexWeightDao get() = localDb.indexWeightDao()
    private val recipeDao get() = localDb.recipeDao()

    private fun getUserId(): String? {
        return user?.uid
    }

    val cloudUpdater: CloudUpdater? get() = user?.let { CloudUpdater(this, it) }

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
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(shoppingListId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .document(item.cloudId!!)
                .delete()
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
        val newCloudId: String = if (existingStoreDoc == null || !existingStoreDoc.exists) {
            firestore.collection(FirestoreCollections.STORES)
                .add(CloudConverter.toSnapshot(store, userId))
                .id
        } else {
            firestore.collection(FirestoreCollections.STORES)
                .document(existingStoreDoc.id)
                .set(CloudConverter.toSnapshot(store, userId))
            existingStoreDoc.id
        }
        storeDao.update(store.copy(cloudId = newCloudId))
        return newCloudId
    }

    suspend fun saveShoppingList(shoppingList: ShoppingList): String {
        val userId = getUserId()!!
        val existingShoppingListSnapshot = getShoppingListSnapshot()

        val newCloudId: String = if (existingShoppingListSnapshot == null) {
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .add(CloudConverter.toSnapshot(shoppingList, userId))
                .id
        } else {
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(existingShoppingListSnapshot.id)
                .set(CloudConverter.toSnapshot(shoppingList, userId))
            existingShoppingListSnapshot.id
        }
        shoppingListDao.update(shoppingList.copy(cloudId = newCloudId))
        return newCloudId
    }

    suspend fun saveSingleItem(singleItem: SingleItem): String {
        val userId = getUserId()!!
        val existingItemSnapshot = singleItem.cloudId?.let { getSingleItemSnapshot(it) }
        val resolvedCloudId: String = if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            firestore.collection(FirestoreCollections.SINGLE_ITEMS)
                .add(CloudConverter.toSnapshot(singleItem, userId))
                .id
        } else {
            firestore.collection(FirestoreCollections.SINGLE_ITEMS)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(singleItem, userId))
            existingItemSnapshot.id
        }
        singleItemDao.updateSingleItem(singleItem.copy(cloudId = resolvedCloudId))
        return resolvedCloudId
    }

    suspend fun saveShoppingListItem(item: ShoppingListItem, listCloudId: String): String {
        val userId = getUserId()!!
        val existingItemSnapshot = item.cloudId?.let { getShoppingListItemSnapshot(listCloudId, it) }
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            resolvedCloudId = firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(listCloudId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .add(CloudConverter.toSnapshot(item, userId, listCloudId))
                .id
        } else {
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(listCloudId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(item, userId, listCloudId))
            resolvedCloudId = existingItemSnapshot.id
        }
        shoppingListItemDao.update(item.copy(cloudId = resolvedCloudId))
        return resolvedCloudId
    }

    suspend fun saveIndexWeight(item: IndexWeight, storeCloudId: String): String {
        val userId = getUserId()!!
        val existingItemSnapshot = item.cloudId?.let { getIndexWeightSnapshot(it) }
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            resolvedCloudId = firestore.collection(FirestoreCollections.INDEX_WEIGHTS)
                .add(CloudConverter.toSnapshot(item, userId, storeCloudId))
                .id
        } else {
            firestore.collection(FirestoreCollections.INDEX_WEIGHTS)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(item, userId, storeCloudId))
            resolvedCloudId = existingItemSnapshot.id
        }
        indexWeightDao.update(item.copy(cloudId = resolvedCloudId))
        return resolvedCloudId
    }

    suspend fun saveRecipe(recipe: Recipe): String {
        val userId = getUserId()!!
        val existingRecipeDoc = recipe.cloudId?.let { getRecipeSnapshot(it) }
        val newCloudId: String = if (existingRecipeDoc == null || !existingRecipeDoc.exists) {
            firestore.collection(FirestoreCollections.RECIPES)
                .add(CloudConverter.toSnapshot(recipe, userId))
                .id
        } else {
            firestore.collection(FirestoreCollections.RECIPES)
                .document(existingRecipeDoc.id)
                .set(CloudConverter.toSnapshot(recipe, userId))
            existingRecipeDoc.id
        }
        recipeDao.updateRecipe(recipe.copy(cloudId = newCloudId))
        return newCloudId
    }

    suspend fun saveRecipeItem(item: RecipeItem, recipeCloudId: String): String {
        val userId = getUserId()!!
        val existingItemSnapshot = item.cloudId?.let { getRecipeItemSnapshot(recipeCloudId, it) }
        val resolvedCloudId: String
        if (existingItemSnapshot == null || !existingItemSnapshot.exists) {
            resolvedCloudId = firestore.collection(FirestoreCollections.RECIPES)
                .document(recipeCloudId)
                .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                .add(CloudConverter.toSnapshot(item, userId, recipeCloudId))
                .id
        } else {
            firestore.collection(FirestoreCollections.RECIPES)
                .document(recipeCloudId)
                .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                .document(existingItemSnapshot.id)
                .set(CloudConverter.toSnapshot(item, userId, recipeCloudId))
            resolvedCloudId = existingItemSnapshot.id
        }
        // RecipeItem does not have a cloudId field
        // recipeItemDao.update(item.copy(cloudId = resolvedCloudId))
        return resolvedCloudId
    }

    suspend fun deleteStore(store: Store) {
        store.cloudId?.let {
            firestore.collection(FirestoreCollections.STORES).document(it).delete()
        }
    }

    suspend fun deleteShoppingListItem(item: ShoppingListItem) {
        item.cloudId?.let {
            val listCloudId = shoppingListDao.getShoppingList(item.listId)?.cloudId ?: return
            firestore.collection(FirestoreCollections.SHOPPING_LISTS)
                .document(listCloudId)
                .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
                .document(it)
                .delete()
        }
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipe.cloudId?.let {
            deleteAllItemsForRecipe(it)
            firestore.collection(FirestoreCollections.RECIPES).document(it).delete()
        }
    }

    suspend fun deleteRecipeItem(item: RecipeItem) {
        item.cloudId?.let {
            val recipeCloudId = recipeDao.getRecipe(item.recipeId)?.cloudId ?: return
            firestore.collection(FirestoreCollections.RECIPES)
                .document(recipeCloudId)
                .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
                .document(it)
                .delete()
        }
    }

    // --- Snapshot Getters ---
    suspend fun getStoreSnapshot(cloudId: String): DocumentSnapshot = firestore.collection(FirestoreCollections.STORES).document(cloudId).get()
    suspend fun getShoppingListSnapshot(cloudId: String): DocumentSnapshot = firestore.collection(FirestoreCollections.SHOPPING_LISTS).document(cloudId).get()
    suspend fun getSingleItemSnapshot(cloudId: String): DocumentSnapshot = firestore.collection(FirestoreCollections.SINGLE_ITEMS).document(cloudId).get()
    suspend fun getShoppingListItemSnapshot(listCloudId: String, cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.SHOPPING_LISTS)
            .document(listCloudId)
            .collection(FirestoreCollections.SHOPPING_LIST_ITEMS_SUBCOLLECTION)
            .document(cloudId)
            .get()
    suspend fun getIndexWeightSnapshot(cloudId: String): DocumentSnapshot = firestore.collection(FirestoreCollections.INDEX_WEIGHTS).document(cloudId).get()
    suspend fun getRecipeSnapshot(cloudId: String): DocumentSnapshot = firestore.collection(FirestoreCollections.RECIPES).document(cloudId).get()
    suspend fun getRecipeItemSnapshot(recipeCloudId: String, cloudId: String): DocumentSnapshot =
        firestore.collection(FirestoreCollections.RECIPES)
            .document(recipeCloudId)
            .collection(FirestoreCollections.RECIPE_ITEMS_SUBCOLLECTION)
            .document(cloudId)
            .get()
}
