package pl.techbrewery.sam.features.shoppinglist

import pl.techbrewery.sam.kmp.database.entity.ShoppingListItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.model.ShoppingItemWithWeight
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.ui.shared.DropdownItem

class ItemChecked(val itemId: Long)
internal class ItemMoved(val from: Int, val to: Int)
class StoreDropdownItemSelected(val dropdownItem: DropdownItem<Store>)
class StoresDropdownVisibilityChanged(val visible: Boolean)
class SuggestedItemSelected(val item: SuggestedItem)
object ItemFieldKeyboardDonePressed
class ShoppingListItemDismissed(val item: ShoppingItemWithWeight)
object AddCollaboratorPressed
object ShareShoppingListPressed