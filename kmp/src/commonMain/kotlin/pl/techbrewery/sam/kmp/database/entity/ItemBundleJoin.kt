package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "item_bundle_join",
    primaryKeys = ["bundle_id_join", "item_name_join"],
    foreignKeys = [
        ForeignKey(
            entity = ItemBundle::class,
            parentColumns = ["bundle_id"],
            childColumns = ["bundle_id_join"],
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
data class ItemBundleJoin(
    @ColumnInfo(name = "bundle_id_join") val bundleId: Long,
    @ColumnInfo(name = "item_name_join", index = true) val itemName: String
)
