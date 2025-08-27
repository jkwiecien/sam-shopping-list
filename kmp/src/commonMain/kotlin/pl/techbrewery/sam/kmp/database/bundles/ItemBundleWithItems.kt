package pl.techbrewery.sam.kmp.database.bundles

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeJoin
import pl.techbrewery.sam.kmp.database.entity.SingleItem

data class RecipeWithItems(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipe_id",
        entityColumn = "item_name",
        associateBy = Junction(
            value = RecipeJoin::class,
            parentColumn = "recipe_id_join",
            entityColumn = "item_name_join"
        )
    )
    val items: List<SingleItem>
)
