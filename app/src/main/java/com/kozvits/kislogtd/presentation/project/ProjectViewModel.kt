package com.kozvits.kislogtd.presentation.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectSummary(
    val name: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val progressPercent: Float
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    // All tasks, used to derive project summaries
    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Project summaries: tasks whose categoryName is all-uppercase and not a known procedural category
    val projectSummaries: StateFlow<List<ProjectSummary>> = allTasks.map { tasks ->
        val projectNames = tasks.mapNotNull { it.categoryName }
            .filter { name ->
                name == name.uppercase() &&
                name.length > 2 &&
                !name.startsWith("*") &&
                !name.startsWith(">") &&
                !name.startsWith("\\") &&
                !name.startsWith("ЯЯ")
            }.distinct().sorted()

        projectNames.map { projName ->
            val projTasks = tasks.filter { it.categoryName == projName || it.projectId == projName }
            val completed = projTasks.count { it.status == TaskStatus.COMPLETED }
            ProjectSummary(
                name = projName,
                totalTasks = projTasks.size,
                completedTasks = completed,
                progressPercent = if (projTasks.isEmpty()) 0f else (completed.toFloat() / projTasks.size) * 100f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTasksForProject(projectName: String): Flow<List<Task>> {
        return taskRepository.getAllTasks().map { tasks ->
            tasks.filter { it.categoryName == projectName || it.projectId == projectName }
        }
    }

    fun createProject(name: String) {
        viewModelScope.launch {
            taskRepository.upsertTask(
                Task(
                    title = "Проект $name",
                    category = TaskCategory.PROJECT,
                    categoryName = name.uppercase(),
                    isStem = false
                )
            )
        }
    }

    fun addTaskToProject(projectName: String, title: String, subjectPrefix: String? = "Я") {
        viewModelScope.launch {
            taskRepository.upsertTask(
                Task(
                    title = title,
                    subjectPrefix = subjectPrefix,
                    category = TaskCategory.DAY,
                    categoryName = "**DAY",
                    projectId = projectName,
                    startDate = System.currentTimeMillis()
                )
            )
        }
    }
}
