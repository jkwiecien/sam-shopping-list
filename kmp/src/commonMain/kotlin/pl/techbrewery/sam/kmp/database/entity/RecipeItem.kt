package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "recipe_item_join",
    primaryKeys = ["recipe_id_join", "item_name_join"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["recipe_id"],
            childColumns = ["recipe_id_join"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SingleItem::class,
            parentColumns = ["item_name"],
            childColumns = ["item_name_join"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeItem(
    @ColumnInfo(name = "cloud_id") val cloudId: String? = null,
    @ColumnInfo(name = "recipe_id_join") val recipeId: Long,
    @ColumnInfo(name = "item_name_join", index = true) val itemName: String,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = createdAt
)
