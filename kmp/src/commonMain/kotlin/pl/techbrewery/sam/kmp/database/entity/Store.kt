package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(tableName = "stores")
data class Store(
    @ColumnInfo(name = "store_id") @PrimaryKey(autoGenerate = true) val storeId: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String = "",
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = createdAt,
    @ColumnInfo(name = "selected") val selected: Boolean = false,
) {
    companion object {
        fun createInitialStore(): Store = Store(0, name = "",selected = true)
    }
}

