package com.kozvits.kislogtd.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kozvits.kislogtd.domain.WeeklyStats

@Entity(tableName = "weekly_stats")
data class WeeklyStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "week_start_date")
    val weekStartDate: Long,

    @ColumnInfo(name = "total_completed")
    val totalCompleted: Int,

    @ColumnInfo(name = "project_completed")
    val projectCompleted: Int,

    @ColumnInfo(name = "project_percent")
    val projectPercent: Float,

    @ColumnInfo(name = "diary_entry")
    val diaryEntry: String
) {
    fun toDomain(): WeeklyStats = WeeklyStats(
        weekStartDate = weekStartDate,
        totalCompleted = totalCompleted,
        projectCompleted = projectCompleted,
        projectPercent = projectPercent,
        diaryEntry = diaryEntry
    )

    companion object {
        fun fromDomain(stats: WeeklyStats): WeeklyStatsEntity = WeeklyStatsEntity(
            weekStartDate = stats.weekStartDate,
            totalCompleted = stats.totalCompleted,
            projectCompleted = stats.projectCompleted,
            projectPercent = stats.projectPercent,
            diaryEntry = stats.diaryEntry
        )
    }
}
