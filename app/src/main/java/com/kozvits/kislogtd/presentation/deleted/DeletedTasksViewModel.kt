package com.kozvits.kislogtd.presentation.deleted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletedTasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val deletedTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByStatus("DELETED")
        .map { tasks -> tasks.sortedByDescending { it.completedAt ?: 0L } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            val restored = task.copy(
                status = TaskStatus.ACTIVE,
                category = TaskCategory.INBOX,
                completedAt = null
            )
            taskRepository.upsertTask(restored)
        }
    }

    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}
