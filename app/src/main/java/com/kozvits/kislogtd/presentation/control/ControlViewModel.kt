package com.kozvits.kislogtd.presentation.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    val controlTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("CONTROL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markDone(task: Task) {
        viewModelScope.launch {
            taskRepository.upsertTask(
                task.copy(
                    status = TaskStatus.COMPLETED,
                    completedAt = System.currentTimeMillis()
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

    fun updateStartDate(task: Task, newDate: Long) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(newDate))
            val newNotes = if (task.notes.isBlank()) {
                "Контроль: $dateStr"
            } else {
                "${task.notes}\nКонтроль: $dateStr"
            }
            taskRepository.upsertTask(task.copy(startDate = newDate, notes = newNotes))
        }
    }

    fun toggleTaskComplete(task: com.kozvits.kislogtd.domain.model.Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(task)
        }
    }
}