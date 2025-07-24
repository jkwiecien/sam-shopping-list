package pl.techbrewery.sam.features.shoppinglist.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import pl.techbrewery.sam.features.stores.StoreItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.PrimaryDropdown

@Composable
internal fun StoresDropdown(
    items: ImmutableList<DropdownItem<Store>>,
    selectedItem: DropdownItem<Store>,
    onItemSelected: (DropdownItem<Store>) -> Unit = {}
) {
    PrimaryDropdown(
        selectedItemText = selectedItem.text,
        dropdownItems = items,
        onSelectedItemChanged = onItemSelected,
        createItem = { dropdownItem ->
            StoreItem(
                store = dropdownItem.item
            )
        },
        modifier = Modifier.Companion.fillMaxWidth()
    )
}