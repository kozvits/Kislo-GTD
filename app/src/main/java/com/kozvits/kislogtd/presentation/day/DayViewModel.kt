package com.kozvits.kislogtd.presentation.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayGroup(
    val subject: String,
    val tasks: List<Task>
)

@HiltViewModel
class DayViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** All active DAY tasks, optionally filtered by search query */
    val dayTasks: StateFlow<List<Task>> = combine(
        taskRepository.getTasksByCategory("DAY"),
        _searchQuery
    ) { tasks, query ->
        val active = tasks.filter { it.status == TaskStatus.ACTIVE }
        if (query.isBlank()) {
            active
        } else {
            active.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.notes.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Tasks grouped into sections: Срочные, subject-prefix groups, Стволовые */
    val groupedTasks: StateFlow<List<DayGroup>> = dayTasks.map { tasks ->
        val urgent = tasks.filter { it.isUrgent }
        val normal = tasks.filter { !it.isUrgent && !it.isStem }
        val stem = tasks.filter { it.isStem }

        val groups = mutableListOf<DayGroup>()
        if (urgent.isNotEmpty()) {
            groups.add(DayGroup("Срочные", urgent))
        }
        // Group normal tasks by subject prefix
        val bySubject = normal.groupBy { it.subjectPrefix ?: "Я" }
        bySubject.toSortedMap().forEach { (subject, subjectTasks) ->
            groups.add(DayGroup(subject, subjectTasks))
        }
        if (stem.isNotEmpty()) {
            // Separate stem tasks — push each to bottom
            groups.add(DayGroup("Стволовые", stem))
        }
        groups
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            if (task.isStem) {
                // Stem task: push startDate forward by one day
                val nextDate = (task.startDate ?: System.currentTimeMillis()) + 86400000L
                taskRepository.upsertTask(
                    task.copy(
                        startDate = nextDate,
                        completedAt = System.currentTimeMillis()
                    )
                )
            } else {
                val now = System.currentTimeMillis()
                taskRepository.upsertTask(
                    task.copy(
                        status = if (task.status == TaskStatus.ACTIVE)
                            TaskStatus.COMPLETED
                        else
                            TaskStatus.ACTIVE,
                        completedAt = if (task.status == TaskStatus.ACTIVE) now else null
                    )
                )
            }
        }
    }

    fun moveTask(task: Task, targetCategoryName: String) {
        viewModelScope.launch {
            val targetCategory = when (targetCategoryName) {
                "**LATER" -> TaskCategory.LATER
                "*CONTROL" -> TaskCategory.CONTROL
                else -> TaskCategory.DAY
            }
            taskRepository.upsertTask(
                task.copy(
                    category = targetCategory,
                    categoryName = targetCategoryName,
                    startDate = if (targetCategoryName == "**LATER") null else task.startDate
                )
            )
        }
    }

    /** Add a new action to DAY */
    fun addDayAction(title: String, subjectPrefix: String? = null) {
        viewModelScope.launch {
            val cleanTitle = if (title.contains("\\")) {
                title.substringAfter("\\")
            } else {
                title
            }
            val prefix = subjectPrefix
                ?: if (title.contains("\\")) title.substringBefore("\\")
                else null

            val task = Task(
                title = cleanTitle,
                subjectPrefix = prefix,
                category = TaskCategory.DAY,
                categoryName = "**DAY",
                startDate = System.currentTimeMillis()
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