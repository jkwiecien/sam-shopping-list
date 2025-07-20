package pl.techbrewery.sam.features.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class NavigationViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {

    var selectedTab: NavigationTab by mutableStateOf(NavigationTab.SHOPPING_LIST)
        private set

    override fun onAction(action: Any) {
        when (action) {
            is NavigationTabPressed -> {
                selectedTab = action.tab
            }
        }
    }
}