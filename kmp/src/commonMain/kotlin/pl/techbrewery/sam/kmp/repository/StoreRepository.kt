package pl.techbrewery.sam.kmp.repository

import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment

class StoreRepository(
    private val kmpDatabase: KmpDatabase
) {
    suspend fun hasAnyStores(): Boolean {
        return kmpDatabase.storeDao().hasAnyStores()
    }

    suspend fun saveStoreLayout(
        departments: List<String>,
        storeName: String
    ) {
        departments
            .map { departmentName ->
                StoreDepartment(
                    departmentName = departmentName
                )
            }.forEach { department ->
                kmpDatabase.storeDepartmentDao().insertStoreDepartment(department)
            }

        val store = Store(
            name = storeName
        )
        kmpDatabase.storeDao().insert(store)
    }
}