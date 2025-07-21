package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.kmp.database.pojo.StoreWithDepartments

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

    suspend fun getStoreWithDepartments(storeId: Long): Flow<StoreWithDepartments?> {
        return kmpDatabase.storeDao().getStoreWithDepartments(storeId)
    }
}