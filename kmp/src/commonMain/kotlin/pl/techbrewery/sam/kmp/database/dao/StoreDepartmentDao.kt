package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment

@Dao
interface StoreDepartmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStoreDepartment(department: StoreDepartment)

    @Query("SELECT * FROM store_departments ORDER BY department_name ASC") // Order by name for consistency
    fun getAllStoreDepartments(): Flow<List<StoreDepartment>>
}