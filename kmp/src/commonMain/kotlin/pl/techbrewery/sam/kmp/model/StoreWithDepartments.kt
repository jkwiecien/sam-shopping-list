package pl.techbrewery.sam.kmp.model

import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment

data class StoreWithDepartments(
    val store: Store,
    val departments: List<StoreDepartment> = emptyList()
)