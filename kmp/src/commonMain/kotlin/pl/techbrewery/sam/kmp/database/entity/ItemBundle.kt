package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(tableName = "item_bundles")
@OptIn(ExperimentalTime::class)
data class ItemBundle (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "bundle_id")  val bundleId: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "createdAt") val createdAt: String = Clock.System.now().toString()
)

