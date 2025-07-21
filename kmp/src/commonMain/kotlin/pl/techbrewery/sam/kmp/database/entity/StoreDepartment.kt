package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(tableName = "store_departments")
data class StoreDepartment(
    @PrimaryKey @ColumnInfo(name = "department_name") val departmentName: String,
    @ColumnInfo(name = "position") val position: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString()
)