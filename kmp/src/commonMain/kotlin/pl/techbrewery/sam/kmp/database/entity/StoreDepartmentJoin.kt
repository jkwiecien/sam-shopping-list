package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "store_department_join",
    primaryKeys = ["department_id_join", "store_id_join"],
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["store_id"], // Matches @ColumnInfo in Store.kt
            childColumns = ["store_id_join"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StoreDepartment::class,
            parentColumns = ["department_name"], // Matches @ColumnInfo in StoreDepartment.kt
            childColumns = ["department_id_join"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StoreDepartmentJoin(
    // This column stores the foreign key from StoreDepartment (which is department_name)
    @ColumnInfo(name = "department_id_join") val departmentKey: String, // Changed type to String
    // This column stores the foreign key from Store (which is store_id)
    @ColumnInfo(name = "store_id_join", index = true) val storeKey: Int // Changed type to Int
)
