package com.kozvits.kislogtd.presentation.later

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kozvits.kislogtd.domain.model.displayTitle
import com.kozvits.kislogtd.presentation.common.components.EmptyState
import com.kozvits.kislogtd.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaterScreen(
    navController: NavController,
    viewModel: LaterViewModel = hiltViewModel()
) {
    val tasks by viewModel.laterTasks.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Schedule,
                message = "Нет отложенных дел.\nВсё разобрано до уровня действий!"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        "**LATER — Отложенные",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(4.dp))
                }

                items(tasks, key = { it.id }) { task ->
                    LaterTaskCard(
                        task = task,
                        onMoveToDay = { viewModel.moveToDay(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun LaterTaskCard(
    task: Task,
    onMoveToDay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = CategoryLater,
                modifier = Modifier.size(4.dp, 36.dp)
            ) {}
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.displayTitle, style = MaterialTheme.typography.bodyLarge)
                if (task.subjectPrefix != null) {
                    Text(
                        task.subjectPrefix!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = CategoryLater.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onMoveToDay) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "В **DAY",
                    tint = CategoryDay
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
