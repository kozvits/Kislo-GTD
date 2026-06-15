package com.kozvits.kislogtd.presentation.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val taskId: String = savedStateHandle.get<String>("taskId") ?: ""

    val task: StateFlow<Task?> = taskRepository.getTaskById(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** All known project names (uppercase categoryName values). */
    val projectNames: StateFlow<List<String>> = taskRepository.getAllTasks().map { tasks ->
        tasks.mapNotNull { it.categoryName }
            .filter { name ->
                name == name.uppercase() && name.length > 2 &&
                !name.startsWith("*") && !name.startsWith(">") && !name.startsWith("\\") && !name.startsWith("ЯЯ")
            }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTitle(newTitle: String) {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(title = newTitle))
        }
    }

    fun updateNotes(newNotes: String) {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(notes = newNotes))
        }
    }

    fun updateCategory(newCategory: String, taskCategory: TaskCategory) {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(category = taskCategory, categoryName = newCategory))
        }
    }

    fun updateProject(projectId: String?) {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(projectId = projectId))
        }
    }

    fun toggleComplete() {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            if (current.status == TaskStatus.ACTIVE) {
                taskRepository.upsertTask(current.copy(status = TaskStatus.COMPLETED, completedAt = System.currentTimeMillis()))
            } else {
                taskRepository.upsertTask(current.copy(status = TaskStatus.ACTIVE, completedAt = null))
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.deleteTask(current)
        }
    }

    fun toggleStem() {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(isStem = !current.isStem))
        }
    }

    fun toggleUrgent() {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(isUrgent = !current.isUrgent))
        }
    }

    fun updateSubjectPrefix(prefix: String) {
        viewModelScope.launch {
            val current = task.value ?: return@launch
            taskRepository.upsertTask(current.copy(subjectPrefix = prefix.ifBlank { null }))
        }
    }
}
