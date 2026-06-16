package com.kozvits.kislogtd.presentation.maybe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaybeViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    val maybeTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("MAYBE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDream(title: String) {
        viewModelScope.launch {
            taskRepository.upsertTask(
                Task(
                    title = title,
                    category = TaskCategory.MAYBE,
                    categoryName = ">>MAYBE"
                )
            )
        }
    }

    fun moveToDay(task: Task) {
        viewModelScope.launch {
            taskRepository.upsertTask(
                task.copy(
                    category = TaskCategory.DAY,
                    categoryName = "**DAY",
                    startDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }

    fun toggleTaskComplete(task: com.kozvits.kislogtd.domain.model.Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(task)
        }
    }
}
