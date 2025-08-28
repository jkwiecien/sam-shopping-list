package pl.techbrewery.sam.kmp.cloud

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.entity.ShoppingList
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.utils.debugLog
import pl.techbrewery.sam.kmp.utils.warningLog

private const val LOG_TAG = "CloudSyncService"

class CloudSyncService(
    private val localDb: KmpDatabase,
    private val cloud: CloudRepository
) {
    private val singleItemDao get() = localDb.singleItemDao()
    private val storeDao get() = localDb.storeDao()
    private val shoppingListItemDao get() = localDb.shoppingListItemDao()
    private val recipeDao get() = localDb.recipeDao()
    private val shoppingListDao get() = localDb.shoppingListDao()
    private val indexWeightDao get() = localDb.indexWeightDao()

    suspend fun syncDatabases() {
        debugLog("Starting database sync", LOG_TAG)
        syncStores()
        syncShoppingLists()
        syncSingleItems()
        syncIndexWeights()
        syncRecipes()
        debugLog("Database sync finished", LOG_TAG)
    }

    private suspend fun syncStores() = coroutineScope {
        debugLog("Syncing stores", LOG_TAG)

        // 1. Push local changes
        val localStores = withContext(Dispatchers.Default) { storeDao.getAllStores() }
        val cloudStoresBeforePush = withContext(Dispatchers.IO) { cloud.getStores() }

        for (localStore in localStores) {
            if (localStore.cloudId == null) {
                val matchingCloudStore = cloudStoresBeforePush.find { cs -> cs.storeName == localStore.storeName }
                val storeToSave = matchingCloudStore?.let { localStore.copy(cloudId = it.cloudId) } ?: localStore
                val savedCloudId = withContext(Dispatchers.IO) { cloud.saveStore(storeToSave) }
                
                withContext(Dispatchers.Default) { storeDao.update(localStore.copy(cloudId = savedCloudId)) }

                debugLog("Pushed local store ${storeToSave.storeName} to cloud with new cloudId $savedCloudId", LOG_TAG)
            } else {
                withContext(Dispatchers.IO) { cloud.saveStore(localStore) }
                debugLog("Pushed updated local store ${localStore.storeName} to cloud.", LOG_TAG)
            }
        }

        // 2. Pull cloud changes
        val cloudStoresAfterPush = withContext(Dispatchers.IO) { cloud.getStores() }
        val finalLocalStores = withContext(Dispatchers.Default) { storeDao.getAllStores() }
        val localCloudIds = finalLocalStores.mapNotNull { it.cloudId }.toSet()

        for (cloudStore in cloudStoresAfterPush) {
            if (cloudStore.cloudId !in localCloudIds) {
                debugLog("Cloud store ${cloudStore.cloudId} not found locally. Inserting into local DB.", LOG_TAG)
                withContext(Dispatchers.Default) { storeDao.insert(cloudStore.copy(storeId = 0)) }
            }
        }
        debugLog("Finished syncing stores", LOG_TAG)
    }

    private suspend fun syncShoppingLists() = coroutineScope {
        debugLog("Syncing shopping lists", LOG_TAG)

        val cloudListSnapshot = withContext(Dispatchers.IO) { cloud.getShoppingListSnapshot() }
        var localList = withContext(Dispatchers.Default) { shoppingListDao.getShoppingList() }

        if (cloudListSnapshot == null) {
            // Cloud has no list. If we have a local one, push it.
            if (localList != null) {
                debugLog("No cloud list found. Pushing local list ${localList.id} to the cloud.", LOG_TAG)
                val newCloudId = withContext(Dispatchers.IO) { cloud.saveShoppingList(localList) }
                val localItems = withContext(Dispatchers.Default) { shoppingListItemDao.getShoppingListItemsForList(localList.id) }
                for (item in localItems) {
                    withContext(Dispatchers.IO) { cloud.saveShoppingListItem(item, newCloudId) }
                }
            } else {
                debugLog("No shopping list to sync, local and cloud are empty.", LOG_TAG)
            }
        } else {
            // Cloud has a list. It's the source of truth.
            if (localList == null) {
                // No local list, create it from the cloud version.
                debugLog("No local list found. Creating one from cloud list ${cloudListSnapshot.id}", LOG_TAG)
                val newLocalList = CloudConverter.shoppingListFromSnapshot(cloudListSnapshot)
                val newLocalId = withContext(Dispatchers.Default) { shoppingListDao.insert(newLocalList.copy(id = 0)) }
                localList = newLocalList.copy(id = newLocalId)
            } else {
                 // We have a local list, make sure its cloudId is up-to-date.
                 if(localList.cloudId != cloudListSnapshot.id) {
                     withContext(Dispatchers.Default) { shoppingListDao.update(localList.copy(cloudId = cloudListSnapshot.id)) }
                 }
            }
            
            // Merge items
            val cloudItems = withContext(Dispatchers.IO) { cloud.getShoppingListItems(cloudListSnapshot.id) }
            val localItems = withContext(Dispatchers.Default) { shoppingListItemDao.getShoppingListItemsForList(localList.id) }
            
            val localItemsMap = localItems.associateBy { it.itemName }
            val cloudItemsMap = cloudItems.associateBy { it.itemName }
            
            // Create a merged map, cloud items win in case of conflict
            val mergedItemsMap = localItemsMap + cloudItemsMap
            
            // Granularly update local DB
            val localItemsToDelete = localItems.filter { it.itemName !in mergedItemsMap }
            for (item in localItemsToDelete) {
                withContext(Dispatchers.Default) { shoppingListItemDao.deleteById(item.id) }
                debugLog("Deleted local item '${item.itemName}' which is no longer in merged list.", LOG_TAG)
            }

            for (mergedItem in mergedItemsMap.values) {
                val localItem = localItemsMap[mergedItem.itemName]
                if (localItem == null) {
                    // Ensure SingleItem exists before inserting ShoppingListItem
                    withContext(Dispatchers.Default) {
                        if (singleItemDao.getSingleItemByName(mergedItem.itemName) == null) {
                            singleItemDao.insertSingleItem(SingleItem(itemName = mergedItem.itemName, cloudId = mergedItem.cloudId))
                        }
                    }
                    val newItem = mergedItem.copy(id = 0, listId = localList.id)
                    withContext(Dispatchers.Default) { shoppingListItemDao.insert(newItem) }
                    debugLog("Inserted new local item '${newItem.itemName}' from merged list.", LOG_TAG)
                } else {
                    if (localItem.checkedOff != mergedItem.checkedOff || localItem.cloudId != mergedItem.cloudId) {
                        withContext(Dispatchers.Default) { shoppingListItemDao.update(localItem.copy(checkedOff = mergedItem.checkedOff, cloudId = mergedItem.cloudId)) }
                        debugLog("Updated local item '${localItem.itemName}' from merged list.", LOG_TAG)
                    }
                }
            }
            
            // Atomically update cloud DB
            withContext(Dispatchers.IO) { cloud.deleteAllItemsForList(cloudListSnapshot.id) }
            val finalLocalItems = withContext(Dispatchers.Default) { shoppingListItemDao.getShoppingListItemsForList(localList.id) }
            for (item in finalLocalItems) {
                withContext(Dispatchers.IO) { cloud.saveShoppingListItem(item, cloudListSnapshot.id) }
            }
        }

        debugLog("Finished syncing shopping lists", LOG_TAG)
    }

    private suspend fun syncSingleItems() = coroutineScope {
        debugLog("Syncing single items", LOG_TAG)

        // 1. Push local changes
        val localSingleItems = withContext(Dispatchers.Default) { singleItemDao.getAllSingleItems() }
        for (localItem in localSingleItems) {
            // Single items are unique by name, so we just save them.
            withContext(Dispatchers.IO) { cloud.saveSingleItem(localItem) }
        }

        // 2. Pull cloud changes
        val cloudSingleItems = withContext(Dispatchers.IO) { cloud.getSingleItems() }
        val finalLocalItems = withContext(Dispatchers.Default) { singleItemDao.getAllSingleItems() }
        val localItemNames = finalLocalItems.map { it.itemName }.toSet()

        for (cloudItem in cloudSingleItems) {
            if (cloudItem.itemName !in localItemNames) {
                debugLog("Cloud single item '${cloudItem.itemName}' not found locally. Inserting into local DB.", LOG_TAG)
                withContext(Dispatchers.Default) { singleItemDao.insertSingleItem(cloudItem) }
            }
        }
        debugLog("Finished syncing single items", LOG_TAG)
    }

    private suspend fun syncIndexWeights() = coroutineScope {
        debugLog("Syncing index weights", LOG_TAG)

        // 1. Push local changes
        val localIndexWeights = withContext(Dispatchers.Default) { indexWeightDao.getAll() }
        for (localWeight in localIndexWeights) {
            val parentStore = withContext(Dispatchers.Default) { storeDao.getStoreById(localWeight.storeId) }
            if (parentStore?.cloudId != null) {
                withContext(Dispatchers.IO) { cloud.saveIndexWeight(localWeight, parentStore.cloudId) }
            } else {
                warningLog("Skipping push for local IndexWeight (id: ${localWeight.id}) because its parent store (id: ${localWeight.storeId}) is not synced or not found.", LOG_TAG)
            }
        }

        // 2. Pull cloud changes
        val cloudIndexWeightSnapshots = withContext(Dispatchers.IO) { cloud.getIndexWeightSnapshots() }
        val finalLocalIndexWeights = withContext(Dispatchers.Default) { indexWeightDao.getAll() }
        val localWeightCloudIds = finalLocalIndexWeights.mapNotNull { it.cloudId }.toSet()

        for (cloudSnapshot in cloudIndexWeightSnapshots) {
            if (cloudSnapshot.id !in localWeightCloudIds) {
                val storeCloudId = cloudSnapshot.get<String>("store_cloud_id")
                val localParentStore = withContext(Dispatchers.Default) { storeDao.getStoreByCloudId(storeCloudId) }

                if (localParentStore != null) {
                    debugLog("Cloud index weight ${cloudSnapshot.id} not found locally. Inserting into local DB.", LOG_TAG)
                    val newIndexWeight = CloudConverter.indexWeightFromSnapshot(cloudSnapshot)
                    withContext(Dispatchers.Default) { indexWeightDao.insert(newIndexWeight.copy(id = 0, storeId = localParentStore.storeId)) }
                } else {
                    warningLog("Skipping pull for cloud IndexWeight (cloudId: ${cloudSnapshot.id}) because its parent store (cloudId: ${storeCloudId}) was not found locally.", LOG_TAG)
                }
            }
        }
        debugLog("Finished syncing index weights", LOG_TAG)
    }

    private suspend fun syncRecipes() = coroutineScope {
        debugLog("Syncing recipes", LOG_TAG)

        // 1. Push local changes
        val localRecipes = withContext(Dispatchers.Default) { recipeDao.getAllRecipes() }
        for (localRecipe in localRecipes) {
            val savedCloudId = withContext(Dispatchers.IO) { cloud.saveRecipe(localRecipe) }
            debugLog("Pushed local recipe ${localRecipe.recipeId} to cloud with new cloudId $savedCloudId", LOG_TAG)
            val localItems = withContext(Dispatchers.Default) { recipeDao.getIngredients(localRecipe.recipeId) }
            for (item in localItems) {
                withContext(Dispatchers.IO) { cloud.saveRecipeItem(item, savedCloudId) }
            }
        }

        // 2. Pull cloud changes
        val cloudRecipeSnapshots = withContext(Dispatchers.IO) { cloud.getRecipeSnapshots() }
        val finalLocalRecipes = withContext(Dispatchers.Default) { recipeDao.getAllRecipes() }
        val localCloudIds = finalLocalRecipes.mapNotNull { it.cloudId }.toSet()

        for (cloudSnapshot in cloudRecipeSnapshots) {
            if (cloudSnapshot.id !in localCloudIds) {
                debugLog("Cloud recipe ${cloudSnapshot.id} not found locally. Inserting into local DB.", LOG_TAG)
                val newLocalRecipe = CloudConverter.recipeFromSnapshot(cloudSnapshot)
                val newLocalId = withContext(Dispatchers.Default) { recipeDao.insertRecipe(newLocalRecipe.copy(recipeId = 0)) }

                val cloudItems = withContext(Dispatchers.IO) { cloud.getRecipeItems(cloudSnapshot.id) }
                for (cloudItem in cloudItems) {
                    val newItem = RecipeItem(cloudId = cloudItem.cloudId, recipeId = newLocalId, itemName = cloudItem.itemName)
                    withContext(Dispatchers.Default) { recipeDao.insertRecipeIngredient(newItem) }
                }
            } else {
                // Recipe exists, sync items
                val localRecipe = finalLocalRecipes.first { it.cloudId == cloudSnapshot.id }
                val cloudItems = withContext(Dispatchers.IO) { cloud.getRecipeItems(cloudSnapshot.id) }
                val localItems = withContext(Dispatchers.Default) { recipeDao.getIngredients(localRecipe.recipeId) }
                val localItemCloudIds = localItems.mapNotNull { it.cloudId }.toSet()

                for (cloudItem in cloudItems) {
                    if (cloudItem.cloudId !in localItemCloudIds) {
                         debugLog("Cloud item ${cloudItem.cloudId} not found locally for recipe ${localRecipe.recipeId}. Inserting.", LOG_TAG)
                         val newItem = RecipeItem(cloudId = cloudItem.cloudId, recipeId = localRecipe.recipeId, itemName = cloudItem.itemName)
                         withContext(Dispatchers.Default) { recipeDao.insertRecipeIngredient(newItem) }
                    }
                }
            }
        }
        debugLog("Finished syncing recipes", LOG_TAG)
    }
}
