package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SingleItem::class,
            parentColumns = ["item_name"],
            childColumns = ["item_name"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "cloud_id") val cloudId: String? = null,
    @ColumnInfo(name = "item_name", index = true) val itemName: String,
    @ColumnInfo(name = "list_id", index = true) val listId: Long,
    @ColumnInfo(name = "checked_off") val checkedOff: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = createdAt
)

@Serializable
data class ShoppingListItemSnapshot(
    @SerialName("owner_id") val ownerId: String,
    @SerialName("list_cloud_id") val listCloudId: String,
    @SerialName("item_name") val itemName: String,
    @SerialName("checked_off") val checkedOff: Boolean,
    @SerialName("created_at") val createdAt: Timestamp,
    @SerialName("updated_at") val updatedAt: Timestamp
)
