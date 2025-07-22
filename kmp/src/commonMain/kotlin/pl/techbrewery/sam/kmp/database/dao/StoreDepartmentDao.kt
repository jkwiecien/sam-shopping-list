package pl.techbrewery.sam.kmp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment

@Dao
interface StoreDepartmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(department: StoreDepartment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(departments: List<StoreDepartment>)

    @Update
    suspend fun update(department: StoreDepartment)

    @Query("SELECT * FROM store_departments WHERE store_id = :storeId ORDER BY position ASC") // Order by name for consistency
    fun getStoreDepartmentsFlow(storeId: Long): Flow<List<StoreDepartment>>

    @Query("SELECT * FROM store_departments WHERE store_id = :storeId ORDER BY position ASC") // Order by name for consistency
    suspend fun getStoreDepartments(storeId: Long): List<StoreDepartment>

    @Query("SELECT * FROM store_departments WHERE store_id = :storeId AND department_name = :departmentName LIMIT 1")
    suspend fun getStoreDepartment(storeId: Long, departmentName: String): StoreDepartment?
}