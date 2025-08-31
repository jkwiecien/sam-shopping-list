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
    tableName = "index_weights",
    foreignKeys = [
        ForeignKey(
            entity = SingleItem::class,
            parentColumns = ["item_name"],
            childColumns = ["item_name"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Store::class,
            parentColumns = ["store_id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IndexWeight(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "cloud_id") val cloudId: String? = null,
    @ColumnInfo(name = "item_name", index = true) val itemName: String,
    @ColumnInfo(name = "store_id", index = true) val storeId: Long,
    @ColumnInfo(name = "weight") val weight: Long = 0,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = createdAt
)

@Serializable
data class IndexWeightSnapshot(
    @SerialName("owner_id") val ownerId: String,
    @SerialName("item_name") val itemName: String,
    @SerialName("store_cloud_id") val storeCloudId: String,
    @SerialName("weight") val weight: Long,
    @SerialName("created_at") val createdAt: Timestamp,
    @SerialName("updated_at") val updatedAt: Timestamp
)
