package pl.techbrewery.sam.features.stores

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.techbrewery.sam.features.navigation.NavigationViewModel
import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoresViewModel(
    private val navigation: NavigationViewModel,
    private val stores: StoreRepository
) : BaseViewModel() {


}