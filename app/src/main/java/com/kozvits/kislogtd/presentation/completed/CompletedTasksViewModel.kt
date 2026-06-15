package com.kozvits.kislogtd.presentation.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CompletedTasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val completedTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByStatus("COMPLETED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun duplicateToInbox(task: Task) {
        viewModelScope.launch {
            val duplicate = task.copy(
                id = UUID.randomUUID().toString(),
                category = TaskCategory.INBOX,
                categoryName = "***IN",
                status = TaskStatus.ACTIVE,
                completedAt = null,
                createdAt = System.currentTimeMillis(),
                startDate = null
            )
            taskRepository.upsertTask(duplicate)
        }
    }
}
