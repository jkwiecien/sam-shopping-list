package pl.techbrewery.sam.features.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.ScreenId

class NavigationViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {
}