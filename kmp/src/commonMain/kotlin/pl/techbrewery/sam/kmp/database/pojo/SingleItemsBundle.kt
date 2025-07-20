package pl.techbrewery.sam.kmp.database.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import pl.techbrewery.sam.kmp.database.entity.ItemBundle
import pl.techbrewery.sam.kmp.database.entity.ItemBundleCrossRef
import pl.techbrewery.sam.kmp.database.entity.SingleItem

class SingleItemsBundle(
    @Embedded
    val bundle: ItemBundle,
    @Relation(
        parentColumn = "bundle_id",
        entityColumn = "item_name",
        associateBy = Junction(ItemBundleCrossRef::class)
    )
    val items: List<SingleItem>
)