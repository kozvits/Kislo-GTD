package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.WeeklyStats
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import java.util.Calendar

interface GetWeeklyStatsUseCase {
    operator fun invoke(tasks: List<Task>): WeeklyStats
}

class GetWeeklyStatsUseCaseImpl : GetWeeklyStatsUseCase {

    override operator fun invoke(tasks: List<Task>): WeeklyStats {
        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = calendar.timeInMillis
        val weekEnd = weekStart + 7 * 24 * 60 * 60 * 1000L

        val completedThisWeek = tasks.filter { task ->
            task.completedAt != null && task.completedAt in weekStart until weekEnd
        }

        val totalCompleted = completedThisWeek.size
        val projectCompleted = completedThisWeek.count { it.category == TaskCategory.PROJECT }
        val projectPercent = if (totalCompleted > 0) {
            (projectCompleted.toFloat() / totalCompleted * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }

        return WeeklyStats(
            weekStartDate = weekStart,
            totalCompleted = totalCompleted,
            projectCompleted = projectCompleted,
            projectPercent = projectPercent,
            diaryEntry = ""
        )
    }
}
