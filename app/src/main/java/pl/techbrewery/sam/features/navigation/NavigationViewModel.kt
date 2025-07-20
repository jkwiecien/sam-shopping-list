package pl.techbrewery.sam.features.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication.Companion.init
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class NavigationViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {

    private val selectedTabMutableFlow: MutableStateFlow<NavigationTab> =
        MutableStateFlow(NavigationTab.SHOPPING_LIST)
    val selectedTabFlow: StateFlow<NavigationTab> = selectedTabMutableFlow
    var screenTitle: String by mutableStateOf(selectedTabMutableFlow.value.screenTitle)
    private set

    init {
        viewModelScope.launch {
            selectedTabMutableFlow.collect { tab ->
                screenTitle = tab.screenTitle
            }
        }
    }

    override fun onAction(action: Any) {
        when (action) {
            is NavigationTabPressed -> {
                selectedTabMutableFlow.value = action.tab
            }
        }
    }
}