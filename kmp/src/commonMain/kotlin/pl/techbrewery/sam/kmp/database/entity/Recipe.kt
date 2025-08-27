package pl.techbrewery.sam.kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(tableName = "recipes", indices = [Index(value = ["name"], unique = true)])
@OptIn(ExperimentalTime::class)
data class Recipe(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "recipe_id") val recipeId: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "updated_at") val updatedAt: String = createdAt
)

