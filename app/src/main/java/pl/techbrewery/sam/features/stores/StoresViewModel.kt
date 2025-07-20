package pl.techbrewery.sam.features.stores

import pl.techbrewery.sam.kmp.repository.StoreRepository
import pl.techbrewery.sam.shared.BaseViewModel

class StoresViewModel(
    private val stores: StoreRepository
) : BaseViewModel() {
}