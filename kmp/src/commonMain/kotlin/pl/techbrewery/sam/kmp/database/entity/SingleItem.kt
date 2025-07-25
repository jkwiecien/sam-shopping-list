package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "single_items",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["store_id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SingleItem(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "item_id") val itemId: Long = 0,
    @ColumnInfo(name = "store_id", index = true) val storeId: Long,
    @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "index_weight") val indexWeight: Long = 1L,
    @ColumnInfo(name = "checked_off") val checkedOff: Boolean = false
)