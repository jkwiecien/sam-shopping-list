package pl.techbrewery.sam.kmp.database.entity

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeSnapshot(
    @SerialName("owner_id") val ownerId: String,
    @SerialName("name") val name: String,
    @SerialName("created_at") val createdAt: Timestamp,
    @SerialName("updated_at") val updatedAt: Timestamp
)
