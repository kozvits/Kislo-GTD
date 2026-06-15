package com.kozvits.kislogtd.presentation.maybe

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
import com.kozvits.kislogtd.presentation.common.components.EmptyState
import com.kozvits.kislogtd.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaybeScreen(
    navController: NavController,
    viewModel: MaybeViewModel = hiltViewModel()
) {
    val tasks by viewModel.maybeTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Add dialog
    if (showAddDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Записать мечту") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("О чём ты мечтаешь?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) {
                        viewModel.addDream(text)
                        text = ""
                        showAddDialog = false
                    }
                }) { Text("В >>MAYBE") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Отмена") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.AutoAwesome,
                message = "Пока нет мечтаний.\nЗапиши свои самые смелые желания!"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 80.dp
                )
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = CategoryMaybe
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            ">>MAYBE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CategoryMaybe
                        )
                    }
                    Text(
                        "Мечты и воздушные замки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                items(tasks, key = { it.id }) { task ->
                    MaybeDreamCard(
                        task = task,
                        onMoveToDay = { viewModel.moveToDay(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = CategoryMaybe,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Новая мечта")
        }
    }
}

@Composable
fun MaybeDreamCard(
    task: Task,
    onMoveToDay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CategoryMaybe.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = CategoryMaybe.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("ru"))
                        .format(java.util.Date(task.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = CategoryMaybe.copy(alpha = 0.6f)
                )
            }
            if (task.notes.isNotBlank()) {
                IconButton(onClick = { /* show notes */ }) {
                    Icon(
                        Icons.Filled.Notes,
                        contentDescription = "Заметки",
                        tint = CategoryMaybe.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onMoveToDay) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = "В **DAY",
                    tint = CategoryDay
                )
            }
        }
    }
}
