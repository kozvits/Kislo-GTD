package com.kozvits.kislogtd.presentation.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CompletedTasksViewModel @Inject constructor(
    taskRepository: TaskRepository
) : ViewModel() {

    val completedTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByStatus("COMPLETED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
