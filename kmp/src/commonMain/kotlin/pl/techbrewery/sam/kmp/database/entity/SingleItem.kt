package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "single_items"
)
data class SingleItem(
    @PrimaryKey @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString()
)
