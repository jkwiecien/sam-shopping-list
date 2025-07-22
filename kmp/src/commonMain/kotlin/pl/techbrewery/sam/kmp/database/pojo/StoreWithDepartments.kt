package pl.techbrewery.sam.kmp.database.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
// ... other imports
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment

data class StoreWithDepartments(
    val store: Store,
    val departments: List<StoreDepartment> = emptyList()
)