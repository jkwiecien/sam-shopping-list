package pl.techbrewery.sam.features.navigation

import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class NavigationViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {
}