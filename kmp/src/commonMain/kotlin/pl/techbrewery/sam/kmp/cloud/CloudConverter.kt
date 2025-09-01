package pl.techbrewery.sam.kmp.cloud

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp
import pl.techbrewery.sam.kmp.database.entity.IndexWeight
import pl.techbrewery.sam.kmp.database.entity.IndexWeightSnapshot
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.entity.RecipeItemSnapshot
import pl.techbrewery.sam.kmp.database.entity.RecipeSnapshot
import pl.techbrewery.sam.kmp.database.entity.ShoppingList
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.ShoppingListItemSnapshot
import pl.techbrewery.sam.kmp.database.entity.ShoppingListSnapshot
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.SingleItemSnapshot
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreSnapshot
import pl.techbrewery.sam.kmp.utils.TimeUtils


internal object CloudConverter {
    fun singleItemFromSnapshot(documentSnapshot: DocumentSnapshot): SingleItem {
        val createdAtTimestamp = documentSnapshot.get<Timestamp?>("created_at")
        val updatedAtTimestamp = documentSnapshot.get<Timestamp?>("updated_at")
        return SingleItem(
            cloudId = documentSnapshot.id,
            itemName = documentSnapshot.get<String>("item_name"),
            createdAt = TimeUtils.timestampToString(createdAtTimestamp),
            updatedAt = TimeUtils.timestampToString(updatedAtTimestamp)
        )
    }

    fun toSnapshot(singleItem: SingleItem, userId: String): SingleItemSnapshot {
        return SingleItemSnapshot(
            ownerId = userId,
            itemName = singleItem.itemName,
            createdAt = TimeUtils.timestampFromString(singleItem.createdAt),
            updatedAt = TimeUtils.timestampFromString(singleItem.updatedAt)
        )
    }

    fun storeFromSnapshot(documentSnapshot: DocumentSnapshot): Store {
        val createdAtTimestamp = documentSnapshot.get<Timestamp?>("created_at")
        val updatedAtTimestamp = documentSnapshot.get<Timestamp?>("updated_at")
        return Store(
            cloudId = documentSnapshot.id,
            storeName = documentSnapshot.get<String>("store_name"),
            address = documentSnapshot.get<String>("address"),
            createdAt = TimeUtils.timestampToString(createdAtTimestamp),
            updatedAt = TimeUtils.timestampToString(updatedAtTimestamp)
        )
    }

    fun toSnapshot(store: Store, userId: String): StoreSnapshot {
        return StoreSnapshot(
            ownerId = userId,
            storeName = store.storeName,
            address = store.address,
            createdAt = TimeUtils.timestampFromString(store.createdAt),
            updatedAt = TimeUtils.timestampFromString(store.createdAt),
        )
    }

    fun shoppingListFromSnapshot(documentSnapshot: DocumentSnapshot): ShoppingList {
        val createdAtTimestamp = documentSnapshot.get<Timestamp?>("created_at")
        val updatedAtTimestamp = documentSnapshot.get<Timestamp?>("updated_at")
        return ShoppingList(
            cloudId = documentSnapshot.id,
            createdAt = TimeUtils.timestampToString(createdAtTimestamp),
            updatedAt = TimeUtils.timestampToString(updatedAtTimestamp)
        )
    }

    fun toSnapshot(shoppingList: ShoppingList, userId: String): ShoppingListSnapshot {
        return ShoppingListSnapshot(
            ownerId = userId,
            createdAt = TimeUtils.timestampFromString(shoppingList.createdAt),
            updatedAt = TimeUtils.timestampFromString(shoppingList.updatedAt)
        )
    }

    fun shoppingListItemFromSnapshot(documentSnapshot: DocumentSnapshot): ShoppingListItem {
        val createdAtTimestamp = documentSnapshot.get<Timestamp?>("created_at")
        val updatedAtTimestamp = documentSnapshot.get<Timestamp?>("updated_at")
        return ShoppingListItem(
            cloudId = documentSnapshot.id,
            itemName = documentSnapshot.get<String>("item_name"),
            listId = 0L, // Placeholder: CloudSyncService will resolve list_cloud_id to local listId
            checkedOff = documentSnapshot.get<Boolean>("checked_off"),
            createdAt = TimeUtils.timestampToString(createdAtTimestamp),
            updatedAt = TimeUtils.timestampToString(updatedAtTimestamp)
        )
    }

    fun toSnapshot(item: ShoppingListItem, userId: String, listCloudId: String): ShoppingListItemSnapshot {
        return ShoppingListItemSnapshot(
            ownerId = userId,
            listCloudId = listCloudId,
            itemName = item.itemName,
            checkedOff = item.checkedOff,
            createdAt = TimeUtils.timestampFromString(item.createdAt),
            updatedAt = TimeUtils.timestampFromString(item.updatedAt)
        )
    }

    fun indexWeightFromSnapshot(documentSnapshot: DocumentSnapshot): IndexWeight {
        val createdAtTimestamp = documentSnapshot.get<Timestamp?>("created_at")
        val updatedAtTimestamp = documentSnapshot.get<Timestamp?>("updated_at")
        return IndexWeight(
            cloudId = documentSnapshot.id,
            shoppingListItemId = 0L, // Placeholder: CloudSyncService will resolve shopping_list_item_cloud_id to local shoppingListItemId
            storeId = 0L, // Placeholder: CloudSyncService will resolve store_cloud_id to local storeId
            weight = documentSnapshot.get<Long>("weight"),
            createdAt = TimeUtils.timestampToString(createdAtTimestamp),
            updatedAt = TimeUtils.timestampToString(updatedAtTimestamp)
        )
    }

    fun toSnapshot(item: IndexWeight, userId: String, storeCloudId: String, shoppingListItemCloudId: String): IndexWeightSnapshot {
        return IndexWeightSnapshot(
            ownerId = userId,
            shoppingListItemCloudId = shoppingListItemCloudId,
            storeCloudId = storeCloudId,
            weight = item.weight,
            createdAt = TimeUtils.timestampFromString(item.createdAt),
            updatedAt = TimeUtils.timestampFromString(item.updatedAt)
        )
    }

    fun recipeFromSnapshot(documentSnapshot: DocumentSnapshot): Recipe {
        return Recipe(
            cloudId = documentSnapshot.id,
            name = documentSnapshot.get<String>("name"),
            createdAt = TimeUtils.timestampToString(documentSnapshot.get<Timestamp?>("created_at")),
            updatedAt = TimeUtils.timestampToString(documentSnapshot.get<Timestamp?>("updated_at"))
        )
    }

    fun toSnapshot(recipe: Recipe, userId: String): RecipeSnapshot {
        return RecipeSnapshot(
            ownerId = userId,
            name = recipe.name,
            createdAt = TimeUtils.timestampFromString(recipe.createdAt),
            updatedAt = TimeUtils.timestampFromString(recipe.updatedAt)
        )
    }

    fun recipeItemFromSnapshot(documentSnapshot: DocumentSnapshot): RecipeItem {
        val createdAtTimestamp = documentSnapshot.get<Timestamp?>("created_at")
        val updatedAtTimestamp = documentSnapshot.get<Timestamp?>("updated_at")
        return RecipeItem(
            cloudId = documentSnapshot.id,
            recipeId = 0L, // Placeholder: CloudSyncService will resolve recipe_cloud_id to local recipeId
            itemName = documentSnapshot.get<String>("item_name"),
            createdAt = TimeUtils.timestampToString(createdAtTimestamp),
            updatedAt = TimeUtils.timestampToString(updatedAtTimestamp)
        )
    }

    fun toSnapshot(item: RecipeItem, userId: String, recipeCloudId: String): RecipeItemSnapshot {
        return RecipeItemSnapshot(
            ownerId = userId,
            recipeCloudId = recipeCloudId,
            itemName = item.itemName,
            createdAt = TimeUtils.timestampFromString(item.createdAt),
            updatedAt = TimeUtils.timestampFromString(item.updatedAt)
        )
    }
}