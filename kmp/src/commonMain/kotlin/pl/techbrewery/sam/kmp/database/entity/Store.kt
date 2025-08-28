package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(tableName = "stores")
data class Store(
    @ColumnInfo(name = "store_id") @PrimaryKey(autoGenerate = true) val storeId: Long = 0,
    @ColumnInfo(name = "cloud_id") val cloudId: String? = null,
    @ColumnInfo(name = "name") val storeName: String,
    @ColumnInfo(name = "address") val address: String = "",
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = createdAt,
    @ColumnInfo(name = "selected") val selected: Boolean = false,
) {
    companion object {
        fun makeDummyStore() = Store(0, storeName = "")
    }
}

@Serializable
data class StoreSnapshot(
    @SerialName("owner_id") val ownerId: String,
    @SerialName("local_id") val localId: Long,
    @SerialName("store_name") val storeName: String,
    @SerialName("address") val address: String,
    @SerialName("created_at") val createdAt: Timestamp,
    @SerialName("updated_at") val updatedAt: Timestamp
)

