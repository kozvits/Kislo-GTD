package com.kozvits.kislogtd.presentation.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskStatus
import com.kozvits.kislogtd.domain.usecase.CaptureTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedTasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val captureTaskUseCase: CaptureTaskUseCase
) : ViewModel() {

    val completedTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByStatus("COMPLETED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun duplicateToInbox(task: Task) {
        viewModelScope.launch {
            val duplicate = captureTaskUseCase(
                title = task.title,
                notes = task.notes,
                category = TaskCategory.INBOX,
                categoryName = "***IN"
            )
            taskRepository.upsertTask(duplicate)
        }
    }
}
