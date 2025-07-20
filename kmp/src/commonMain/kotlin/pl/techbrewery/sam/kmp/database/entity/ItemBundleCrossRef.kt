package pl.techbrewery.sam.kmp.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

//@Entity(
//    tableName = "item_bundle_cross_ref",
//    primaryKeys = ["bundle_id", "item_name"],
//    foreignKeys = [
//        ForeignKey(
//            entity = ItemBundle::class,
//            parentColumns = ["bundle_id"],
//            childColumns = ["bundle_id"],
//            onDelete = CASCADE
//        ),
//        ForeignKey(
//            entity = SingleItem::class,
//            parentColumns = ["item_name"],
//            childColumns = ["item_name"],
//            onDelete = CASCADE
//        )
//    ]
//)
//data class ItemBundleCrossRef(
//    val bundleId: Long,
//    val itemName: String
//)
