package pl.techbrewery.sam.kmp.model

import pl.techbrewery.sam.kmp.database.entity.SingleItem

enum class SuggestedItemType {
    ITEM,
    RECIPE
}

data class SuggestedItem(
    val itemName: String,
    val type: SuggestedItemType
) {
    fun toSingleItem(): SingleItem = SingleItem(itemName)
}