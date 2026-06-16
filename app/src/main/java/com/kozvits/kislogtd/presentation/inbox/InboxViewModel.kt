package com.kozvits.kislogtd.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val inboxTasks: StateFlow<List<Task>> = taskRepository
        .getTasksByCategory("INBOX")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun processTask(task: Task, newTitle: String, targetCategory: String = "**DAY") {
        viewModelScope.launch {
            val targetCategoryEnum = when (targetCategory) {
                "**DAY" -> TaskCategory.DAY
                "**LATER" -> TaskCategory.LATER
                "*CONTROL" -> TaskCategory.CONTROL
                ">>MAYBE" -> TaskCategory.MAYBE
                else -> TaskCategory.DAY
            }
            // Determine subject prefix from title format "Subject\action"
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

            val processed = task.copy(
                title = cleanTitle,
                subjectPrefix = subjectPrefix,
                category = targetCategoryEnum,
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
            taskRepository.deleteTask(task)
        }
    }

    fun addToInbox(title: String) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                category = TaskCategory.INBOX,
                categoryName = "***IN"
            )
            taskRepository.upsertTask(task)
        }
    }

    fun toggleTaskComplete(task: com.kozvits.kislogtd.domain.model.Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskComplete(task)
        }
    }
}
