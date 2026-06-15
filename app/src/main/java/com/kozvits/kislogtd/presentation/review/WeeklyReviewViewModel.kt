package com.kozvits.kislogtd.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.data.repository.WeeklyStatsRepository
import com.kozvits.kislogtd.domain.WeeklyStats
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class WeeklyReviewState(
    val weekStart: Long = 0,
    val weekEnd: Long = 0,
    val totalCompleted: Int = 0,
    val projectCompleted: Int = 0,
    val projectPercent: Float = 0f,
    val history: List<WeeklyStats> = emptyList(),
    val diaryEntry: String = "",
    val maybeItems: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val weeklyStatsRepository: WeeklyStatsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WeeklyReviewState())
    val state: StateFlow<WeeklyReviewState> = _state.asStateFlow()

    init {
        calculateStats()
    }

    private fun calculateStats() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis
        val weekEnd = weekStart + 7 * 24 * 60 * 60 * 1000L

        viewModelScope.launch {
            val completed = taskRepository.getCompletedBetween(weekStart, weekEnd)
            val completedTasks = completed.filter { it.status == TaskStatus.COMPLETED }
            val total = completedTasks.size
            val projTasks = completedTasks.count { task ->
                task.projectId != null || (
                    task.categoryName != null &&
                    task.categoryName == task.categoryName?.uppercase() &&
                    task.categoryName?.length!! > 2 &&
                    !task.categoryName!!.startsWith("*") &&
                    !task.categoryName!!.startsWith(">") &&
                    !task.categoryName!!.startsWith("\\") &&
                    !task.categoryName!!.startsWith("ЯЯ")
                )
            }
            val percent = if (total > 0) (projTasks.toFloat() / total * 100f) else 0f

            _state.update {
                it.copy(
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    totalCompleted = total,
                    projectCompleted = projTasks,
                    projectPercent = percent,
                    isLoading = false
                )
            }
        }

        // Get maybe items
        viewModelScope.launch {
            taskRepository.getTasksByCategory("MAYBE").collect { items ->
                _state.update { it.copy(maybeItems = items.filter { t -> t.status == TaskStatus.ACTIVE }) }
            }
        }

        // Load history
        viewModelScope.launch {
            weeklyStatsRepository.getAllStats().collect { stats ->
                _state.update { it.copy(history = stats) }
            }
        }
    }

    fun saveDiaryEntry(text: String) {
        _state.update { it.copy(diaryEntry = text) }
    }

    fun saveWeeklyStats() {
        viewModelScope.launch {
            val s = _state.value
            weeklyStatsRepository.saveStats(
                WeeklyStats(
                    weekStartDate = s.weekStart,
                    totalCompleted = s.totalCompleted,
                    projectCompleted = s.projectCompleted,
                    projectPercent = s.projectPercent,
                    diaryEntry = s.diaryEntry
                )
            )
        }
    }
}
