package pl.techbrewery.sam.kmp.database.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import pl.techbrewery.sam.kmp.database.entity.ItemBundle
import pl.techbrewery.sam.kmp.database.entity.ItemBundleJoin
import pl.techbrewery.sam.kmp.database.entity.SingleItem

data class ItemBundleWithItems(
    @Embedded val bundle: ItemBundle,
    @Relation(
        parentColumn = "bundle_id",
        entityColumn = "item_name",
        associateBy = Junction(
            value = ItemBundleJoin::class,
            parentColumn = "bundle_id_join",
            entityColumn = "item_name_join"
        )
    )
    val items: List<SingleItem>
)
