package pl.techbrewery.sam.kmp.database.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
// ... other imports
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.kmp.database.entity.StoreDepartmentJoin

data class StoreWithDepartments(
    @Embedded val store: Store,
    @Relation(
        parentColumn = "store_id",                 // Primary key of Store (from @ColumnInfo name)
        entityColumn = "department_name",          // Primary key of StoreDepartment (from @ColumnInfo name)
        associateBy = Junction(
            value = StoreDepartmentJoin::class,
            parentColumn = "store_id_join",        // Column in StoreDepartmentJoin that references Store's PK
            entityColumn = "department_id_join"    // Column in StoreDepartmentJoin that references StoreDepartment's PK
        )
    )
    val departments: List<StoreDepartment>
)