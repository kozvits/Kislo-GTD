package com.kozvits.kislogtd.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val inboxCount: Int = 0,
    val dayCount: Int = 0,
    val controlCount: Int = 0,
    val laterCount: Int = 0,
    val maybeCount: Int = 0,
    val projectCount: Int = 0,
    val todayCompleted: Int = 0,
    val deletedCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getAllTasks().map { tasks ->
                val active = tasks.filter { it.status == TaskStatus.ACTIVE }
                val now = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis
                val todayEnd = now + 86400000L

                DashboardUiState(
                    inboxCount = active.count { it.category == TaskCategory.INBOX },
                    dayCount = active.count { it.category == TaskCategory.DAY },
                    controlCount = active.count { it.category == TaskCategory.CONTROL },
                    laterCount = active.count { it.category == TaskCategory.LATER },
                    maybeCount = active.count { it.category == TaskCategory.MAYBE },
                    projectCount = active.count { it.category == TaskCategory.PROJECT },
                    todayCompleted = tasks.count {
                        it.status == TaskStatus.COMPLETED
                            && it.completedAt != null
                            && it.completedAt!! >= now
                            && it.completedAt!! < todayEnd
                    },
                    deletedCount = tasks.count { it.status == TaskStatus.DELETED },
                    isLoading = false
                )
            }.catch { e ->
                emit(DashboardUiState(isLoading = false))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addQuickTask(title: String) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                category = TaskCategory.INBOX,
                categoryName = "***IN"
            )
            taskRepository.upsertTask(task)
        }
    }
}
