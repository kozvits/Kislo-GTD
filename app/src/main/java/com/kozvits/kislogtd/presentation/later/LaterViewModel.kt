package com.kozvits.kislogtd.presentation.later

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaterViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    val laterTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("LATER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}
