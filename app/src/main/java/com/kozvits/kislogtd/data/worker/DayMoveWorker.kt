package com.kozvits.kislogtd.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kozvits.kislogtd.data.db.dao.TaskDao
import com.kozvits.kislogtd.domain.model.TaskCategory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Daily worker that moves tasks with [startDate] matching today into **DAY.
 * Runs once per day around midnight, then reschedules itself for the next day.
 */
@HiltWorker
class DayMoveWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskDao: TaskDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayStart = Calendar.getInstance(TimeZone.getDefault()).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todayEnd = todayStart + 86_400_000L // +24h

            val scheduled = taskDao.getScheduledForDate(todayStart, todayEnd)

            for (task in scheduled) {
                taskDao.upsert(task.copy(category = TaskCategory.DAY.name, categoryName = "**DAY"))
            }

            // Reschedule for next midnight
            reschedulePeriodic(applicationContext)

            Result.success()
        } catch (e: Exception) {
            // On error, still try to reschedule so we don't lose the schedule entirely
            reschedulePeriodic(applicationContext)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "day_move_worker"

        fun reschedulePeriodic(context: Context) {
            val now = System.currentTimeMillis()
            val nextMidnight = Calendar.getInstance(TimeZone.getDefault()).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
            val delay = nextMidnight - now

            val request = OneTimeWorkRequestBuilder<DayMoveWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }
}
