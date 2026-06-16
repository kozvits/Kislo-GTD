package com.kozvits.kislogtd.presentation.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import com.kozvits.kislogtd.domain.usecase.CompleteTaskUseCase
import com.kozvits.kislogtd.domain.usecase.MoveTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase
) : ViewModel() {
    val controlTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("CONTROL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markDone(task: Task) {
        viewModelScope.launch {
            val completed = completeTaskUseCase(task)
            taskRepository.upsertTask(completed)
        }
    }

    fun moveToDay(task: Task) {
        viewModelScope.launch {
            val moved = moveTaskUseCase(task, "**DAY")
            taskRepository.upsertTask(moved.copy(startDate = System.currentTimeMillis()))
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

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(task)
        }
    }
}