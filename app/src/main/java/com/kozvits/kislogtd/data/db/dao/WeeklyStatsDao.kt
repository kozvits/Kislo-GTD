package com.kozvits.kislogtd.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kozvits.kislogtd.data.db.entity.WeeklyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyStatsDao {

    @Query("SELECT * FROM weekly_stats ORDER BY week_start_date DESC")
    fun getAll(): Flow<List<WeeklyStatsEntity>>

    @Query("SELECT * FROM weekly_stats WHERE week_start_date = :weekStart")
    suspend fun getByWeek(weekStart: Long): WeeklyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: WeeklyStatsEntity)

    @Query("SELECT * FROM weekly_stats ORDER BY week_start_date DESC LIMIT 1")
    suspend fun getLatest(): WeeklyStatsEntity?

    // ── Sync support ─────────────────────────────────────────────────

    @Query("SELECT * FROM weekly_stats")
    suspend fun getAllStatsList(): List<WeeklyStatsEntity>

    @Query("DELETE FROM weekly_stats")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<WeeklyStatsEntity>)

    @androidx.room.Transaction
    suspend fun replaceAll(stats: List<WeeklyStatsEntity>) {
        deleteAll()
        insertAll(stats)
    }
}
