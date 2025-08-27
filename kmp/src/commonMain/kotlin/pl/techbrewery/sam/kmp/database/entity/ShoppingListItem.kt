package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["store_id"],
            childColumns = ["store_id"],
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
    @ColumnInfo(name = "store_id", index = true) val storeId: Long,
    @ColumnInfo(name = "index_weight") val indexWeight: Long = 0,
    @ColumnInfo(name = "checked_off") val checkedOff: Boolean = false
)
