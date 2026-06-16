package com.kozvits.kislogtd.presentation.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.presentation.common.components.EmptyState
import com.kozvits.kislogtd.presentation.common.components.TaskCard
import com.kozvits.kislogtd.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    navController: NavController,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val tasks by viewModel.getTasksForProject(projectId).collectAsState(initial = null)
    val projects by viewModel.projectSummaries.collectAsState()
    val summary = projects.find { it.name == projectId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projectId) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (tasks == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val projectTasks = tasks!!

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Progress header
            if (summary != null && projectTasks.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CategoryProject.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Прогресс: ${summary.completedTasks}/${summary.totalTasks} (${summary.progressPercent.toInt()}%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CategoryProject
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { summary.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = CategoryProject,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            if (projectTasks.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Workspaces,
                    message = "В этом проекте пока нет задач."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(projectTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onClick = {
                                navController.navigate("task/${task.id}")
                            },
                            onLongClick = {
                                navController.navigate("task/${task.id}")
                            },
                            onCheckboxToggle = { viewModel.toggleTaskComplete(task) }
                        )
                    }
                }
            }
        }
    }
}
