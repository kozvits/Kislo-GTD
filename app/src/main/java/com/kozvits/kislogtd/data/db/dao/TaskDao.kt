package com.kozvits.kislogtd.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kozvits.kislogtd.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY sort_order ASC, created_at DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :categoryName ORDER BY sort_order ASC, created_at DESC")
    fun getByCategory(categoryName: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getById(id: String): Flow<TaskEntity?>

    @Query(
        "SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' " +
        "ORDER BY sort_order ASC, created_at DESC"
    )
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, completed_at = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, completedAt: Long?)

    @Query("SELECT * FROM tasks WHERE completed_at IS NOT NULL AND completed_at >= :start AND completed_at < :end")
    suspend fun getCompletedBetween(start: Long, end: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY sort_order ASC, created_at DESC")
    fun getByProjectId(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY completed_at DESC, created_at DESC")
    fun getByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    suspend fun getCountByStatus(status: String): Int

    // ── Sync support ─────────────────────────────────────────────────

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksList(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @androidx.room.Transaction
    suspend fun replaceAll(tasks: List<TaskEntity>) {
        deleteAll()
        for (task in tasks) {
            upsert(task)
        }
    }
}
