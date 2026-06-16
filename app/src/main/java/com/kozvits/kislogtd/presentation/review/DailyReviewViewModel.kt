package com.kozvits.kislogtd.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import com.kozvits.kislogtd.domain.usecase.CompleteTaskUseCase
import com.kozvits.kislogtd.domain.usecase.MoveTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyReviewState(
    val step: Int = 1,
    val totalSteps: Int = 5,
    val inboxItems: List<Task> = emptyList(),
    val dayItems: List<Task> = emptyList(),
    val controlItems: List<Task> = emptyList(),
    val projects: List<String> = emptyList(),
    val currentInboxIndex: Int = 0,
    val isComplete: Boolean = false
)

@HiltViewModel
class DailyReviewViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(DailyReviewState())
    val state: StateFlow<DailyReviewState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            taskRepository.getTasksByCategory("INBOX").collect { inbox ->
                _state.update { it.copy(inboxItems = inbox.filter { t -> t.status == TaskStatus.ACTIVE }) }
            }
        }
        viewModelScope.launch {
            taskRepository.getTasksByCategory("DAY").collect { day ->
                _state.update { it.copy(dayItems = day.filter { t -> t.status == TaskStatus.ACTIVE }) }
            }
        }
        viewModelScope.launch {
            taskRepository.getTasksByCategory("CONTROL").collect { control ->
                _state.update { it.copy(controlItems = control.filter { t -> t.status == TaskStatus.ACTIVE }) }
            }
        }
    }

    fun processInboxItem(item: Task, newTitle: String, targetCategory: String = "**DAY") {
        viewModelScope.launch {
            val moved = moveTaskUseCase(item, targetCategory)
            val processed = moved.copy(
                title = newTitle,
                categoryName = targetCategory,
                notes = if (item.notes.isBlank()) item.title else "${item.title}\n${item.notes}",
                startDate = if (targetCategory == "**DAY") System.currentTimeMillis() else null
            )
            taskRepository.upsertTask(processed)
            _state.update { it.copy(currentInboxIndex = it.currentInboxIndex + 1) }
        }
    }

    fun skipInboxItem(item: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(item)
            _state.update { it.copy(currentInboxIndex = it.currentInboxIndex + 1) }
        }
    }

    fun moveToNextStep() {
        _state.update { it.copy(step = it.step + 1) }
    }

    fun completeReview() {
        _state.update { it.copy(isComplete = true) }
    }

    fun moveTaskToLater(task: Task) {
        viewModelScope.launch {
            val moved = moveTaskUseCase(task, "**LATER")
            taskRepository.upsertTask(moved)
        }
    }

    fun moveTaskToControl(task: Task) {
        viewModelScope.launch {
            val moved = moveTaskUseCase(task, "*CONTROL")
            taskRepository.upsertTask(moved)
        }
    }

    fun completeControlTask(task: Task) {
        viewModelScope.launch {
            val completed = completeTaskUseCase(task)
            taskRepository.upsertTask(completed)
        }
    }

    fun reset() {
        _state.update { DailyReviewState() }
        loadData()
    }
}
