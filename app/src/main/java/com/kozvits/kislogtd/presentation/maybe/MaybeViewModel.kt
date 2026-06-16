package com.kozvits.kislogtd.presentation.maybe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import com.kozvits.kislogtd.domain.usecase.CaptureTaskUseCase
import com.kozvits.kislogtd.domain.usecase.MoveTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaybeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val captureTaskUseCase: CaptureTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase
) : ViewModel() {
    val maybeTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("MAYBE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDream(title: String) {
        viewModelScope.launch {
            val task = captureTaskUseCase(
                title = title,
                category = TaskCategory.MAYBE,
                categoryName = ">>MAYBE"
            )
            taskRepository.upsertTask(task)
        }
    }

    fun moveToDay(task: Task) {
        viewModelScope.launch {
            val moved = moveTaskUseCase(task, "**DAY")
            taskRepository.upsertTask(moved.copy(startDate = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.softDeleteTask(task) }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(task)
        }
    }
}
