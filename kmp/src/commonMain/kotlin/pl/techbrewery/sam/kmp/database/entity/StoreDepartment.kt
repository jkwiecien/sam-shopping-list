package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "store_departments",
    foreignKeys = [ForeignKey(
        entity = Store::class,
        parentColumns = arrayOf("store_id"),
        childColumns = arrayOf("store_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class StoreDepartment(
    @ColumnInfo(name = "department_id") @PrimaryKey(autoGenerate = true) val departmentId: Long = 0,
    @ColumnInfo(name = "department_name") val departmentName: String,
    @ColumnInfo(name = "store_id", index = true) val storeId: Long,
    @ColumnInfo(name = "position") val position: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString()
)