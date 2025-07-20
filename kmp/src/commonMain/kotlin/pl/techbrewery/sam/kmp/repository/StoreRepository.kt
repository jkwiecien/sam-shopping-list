package pl.techbrewery.sam.kmp.repository

import pl.techbrewery.sam.kmp.database.KmpDatabase

class StoreRepository(
    private val kmpDatabase: KmpDatabase
) {
    suspend fun hasAnyStores(): Boolean {
        return kmpDatabase.storeDao().hasAnyStores()
    }
}