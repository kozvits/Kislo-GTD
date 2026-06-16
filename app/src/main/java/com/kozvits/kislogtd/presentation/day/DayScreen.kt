package com.kozvits.kislogtd.presentation.day

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
import com.kozvits.kislogtd.domain.model.displayTitle
import com.kozvits.kislogtd.presentation.common.components.EmptyState
import com.kozvits.kislogtd.presentation.common.components.TaskCard
import com.kozvits.kislogtd.presentation.navigation.Screen
import com.kozvits.kislogtd.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    navController: NavController,
    viewModel: DayViewModel = hiltViewModel()
) {
    val groupedTasks by viewModel.groupedTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var searchActive by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf<Task?>(null) }
    var addText by remember { mutableStateOf("") }

    // Move dialog
    if (showMoveDialog != null) {
        val task = showMoveDialog!!
        AlertDialog(
            onDismissRequest = { showMoveDialog = null },
            title = { Text("Переместить задачу") },
            text = {
                Text("\"${task.displayTitle}\"")
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            viewModel.moveTask(task, "**LATER")
                            showMoveDialog = null
                        }
                    ) { Text("В **LATER") }
                    TextButton(
                        onClick = {
                            viewModel.moveTask(task, "*CONTROL")
                            showMoveDialog = null
                        }
                    ) { Text("В *CONTROL") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveDialog = null }) { Text("Отмена") }
            }
        )
    }

    // Add dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; addText = "" },
            title = { Text("Новое действие в **DAY") },
            text = {
                Column {
                    Text(
                        "Формат: Имя\\глагол объект",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addText,
                        onValueChange = { addText = it },
                        placeholder = { Text("Степанов\\обсудить отчёт") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (addText.isNotBlank()) {
                            viewModel.addDayAction(addText)
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Search bar (conditionally shown)
            if (searchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    placeholder = { Text("Поиск…") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Очистить")
                            }
                        }
                        IconButton(
                            onClick = {
                                searchActive = false
                                viewModel.setSearchQuery("")
                            }
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Закрыть поиск")
                        }
                    },
                    singleLine = true
                )
            }

            // Content area
            if (groupedTasks.isEmpty() && !searchActive) {
                EmptyState(
                    icon = Icons.Filled.Today,
                    message = "Нет текущих действий на сегодня.\nДобавьте что-нибудь!"
                )
            } else if (groupedTasks.isEmpty() && searchActive) {
                EmptyState(
                    icon = Icons.Filled.SearchOff,
                    message = "Ничего не найдено"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    groupedTasks.forEach { group ->
                        // Group header
                        item(key = "header_${group.subject}") {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.subject,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (group.subject) {
                                        "Срочные" -> CategoryUrgent
                                        "Стволовые" -> CategoryStem
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                // Count badge
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        "${group.tasks.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (group.subject == "Срочные") {
                                    Spacer(Modifier.weight(1f))
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .width(60.dp),
                                        color = CategoryUrgent.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }

                        // Task items in this group
                        items(group.tasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onClick = {
                                    navController.navigate("task/${task.id}")
                                },
                                onLongClick = {
                                    navController.navigate("task/${task.id}")
                                },
                                onCheckboxToggle = { viewModel.toggleComplete(task) },
                                onSwipeLeft = { viewModel.moveTask(task, "**LATER") },
                                onSwipeRight = { viewModel.toggleComplete(task) }
                            )
                        }
                    }
                }
            }
        }

        // Search toggle button (top-right when search is hidden)
        if (!searchActive) {
            SmallFloatingActionButton(
                onClick = { searchActive = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 12.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Поиск")
            }
        }

        // FAB to add new action
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = CategoryDay,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить действие")
        }
    }
}
