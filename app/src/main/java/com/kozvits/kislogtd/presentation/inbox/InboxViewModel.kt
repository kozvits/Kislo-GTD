package com.kozvits.kislogtd.presentation.inbox

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
class InboxViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val captureTaskUseCase: CaptureTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase
) : ViewModel() {

    val inboxTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("INBOX")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun processTask(task: Task, newTitle: String, targetCategory: String = "**DAY") {
        viewModelScope.launch {
            val subjectPrefix = if (newTitle.contains("\\")) {
                newTitle.substringBefore("\\")
            } else {
                null
            }
            val cleanTitle = if (newTitle.contains("\\")) {
                newTitle.substringAfter("\\")
            } else {
                newTitle
            }

            val moved = moveTaskUseCase(task, targetCategory)
            val processed = moved.copy(
                title = cleanTitle,
                subjectPrefix = subjectPrefix,
                categoryName = targetCategory,
                notes = if (task.notes.isBlank()) {
                    task.title
                } else {
                    "${task.title}\n${task.notes}"
                },
                startDate = if (targetCategory == "**DAY") System.currentTimeMillis() else task.startDate,
                status = TaskStatus.ACTIVE
            )
            taskRepository.upsertTask(processed)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.softDeleteTask(task)
        }
    }

    fun addToInbox(title: String) {
        viewModelScope.launch {
            val task = captureTaskUseCase(title = title)
            taskRepository.upsertTask(task)
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(task)
        }
    }
}
