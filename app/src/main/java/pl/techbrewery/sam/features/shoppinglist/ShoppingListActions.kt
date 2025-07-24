package pl.techbrewery.sam.features.shoppinglist

import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.ui.shared.DropdownItem

class ItemChecked(val itemName: String)
internal class ItemMoved(val from: Int, val to: Int)
class StoreDropdownItemSelected(val dropdownItem: DropdownItem<Store>)
class ShoppingListScrollChanged(val scrolled: Boolean)
class SuggestedItemSelected(val item: SingleItem)
object ItemFieldKeyboardDonePressed