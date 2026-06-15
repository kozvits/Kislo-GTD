package com.kozvits.kislogtd.presentation.inbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    navController: NavController,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val tasks by viewModel.inboxTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showProcessDialog by remember { mutableStateOf<Task?>(null) }
    var processTitle by remember { mutableStateOf("") }
    var processCategory by remember { mutableStateOf("**DAY") }
    var addText by remember { mutableStateOf("") }

    // Add dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; addText = "" },
            title = { Text("Захват в ***IN") },
            text = {
                OutlinedTextField(
                    value = addText,
                    onValueChange = { addText = it },
                    placeholder = { Text("Любая мысль, идея, заметка…") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (addText.isNotBlank()) {
                            viewModel.addToInbox(addText)
                            addText = ""
                            showAddDialog = false
                        }
                    }
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; addText = "" }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Process dialog
    if (showProcessDialog != null) {
        val task = showProcessDialog!!
        AlertDialog(
            onDismissRequest = { showProcessDialog = null; processTitle = "" },
            title = { Text("Обработать запись") },
            text = {
                Column {
                    Text(
                        "Исходная запись:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = processTitle,
                        onValueChange = { processTitle = it },
                        label = { Text("Элементарное действие") },
                        placeholder = { Text("Имя\\глагол объект") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Переместить в:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = processCategory == "**DAY",
                            onClick = { processCategory = "**DAY" },
                            label = { Text("**DAY") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = processCategory == "**LATER",
                            onClick = { processCategory = "**LATER" },
                            label = { Text("**LATER") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = processCategory == "*CONTROL",
                            onClick = { processCategory = "*CONTROL" },
                            label = { Text("*CONTROL") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (processTitle.isNotBlank()) {
                            viewModel.processTask(task, processTitle, processCategory)
                            processTitle = ""
                            showProcessDialog = null
                        }
                    }
                ) { Text("Обработать") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProcessDialog = null; processTitle = "" }
                ) { Text("Отмена") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Inbox,
                message = "Входящий поток пуст.\nЗапиши сюда всё, что приходит в голову!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    InboxTaskCard(
                        task = task,
                        onProcess = {
                            processTitle = task.title
                            showProcessDialog = task
                        },
                        onDelete = { viewModel.deleteTask(task) },
                        onLongClick = { navController.navigate("task/${task.id}") }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = CategoryInbox,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Захватить")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InboxTaskCard(
    task: Task,
    onProcess: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .combinedClickable(
                onClick = { /* no-op, card buttons handle actions */ },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color indicator
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = CategoryInbox,
                modifier = Modifier.size(4.dp, 36.dp)
            ) {}
            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = SimpleDateFormat("dd.MM HH:mm", Locale("ru"))
                        .format(Date(task.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Process button
            IconButton(onClick = onProcess) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Обработать",
                    tint = CategoryDay
                )
            }
            // Delete button
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
