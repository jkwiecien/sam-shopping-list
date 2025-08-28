package pl.techbrewery.sam.kmp.database.entity

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeItemSnapshot(
    @SerialName("cloud_id") val cloudId: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("recipe_cloud_id") val recipeCloudId: String,
    @SerialName("item_name") val itemName: String,
    @SerialName("created_at") val createdAt: Timestamp,
    @SerialName("updated_at") val updatedAt: Timestamp
)
