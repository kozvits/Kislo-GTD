package com.kozvits.kislogtd.presentation.taskdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.kozvits.kislogtd.domain.model.*
import com.kozvits.kislogtd.presentation.common.components.CategoryIcon
import com.kozvits.kislogtd.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val task by viewModel.task.collectAsState()
    var editTitle by remember(task?.id) { mutableStateOf(task?.title ?: "") }
    var editNotes by remember(task?.id) { mutableStateOf(task?.notes ?: "") }
    var editSubject by remember(task?.id) { mutableStateOf(task?.subjectPrefix ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить задачу?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTask(); navController.popBackStack() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") } }
        )
    }

    // Date picker dialog
    if (showDatePicker && task != null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = task!!.startDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(task?.id) {
        editTitle = task?.title ?: ""
        editNotes = task?.notes ?: ""
        editSubject = task?.subjectPrefix ?: ""
    }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val t = task!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задача") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleComplete() }) {
                        Icon(
                            if (t.status == TaskStatus.COMPLETED) Icons.Filled.Undo else Icons.Filled.CheckCircle,
                            contentDescription = if (t.status == TaskStatus.COMPLETED) "Отменить" else "Выполнено",
                            tint = if (t.status == TaskStatus.COMPLETED) CategoryDay else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Status badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (t.status == TaskStatus.COMPLETED) CategoryDay.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        if (t.status == TaskStatus.COMPLETED) "\u2713 Выполнено" else "Активно",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (t.status == TaskStatus.COMPLETED) CategoryDay else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                CategoryIcon(t.categoryName ?: t.category.name, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    t.categoryName ?: t.category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = editTitle,
                onValueChange = { editTitle = it; viewModel.updateTitle(it) },
                label = { Text("Действие") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(12.dp))

            // Subject prefix
            OutlinedTextField(
                value = editSubject,
                onValueChange = { editSubject = it; viewModel.updateSubjectPrefix(it) },
                label = { Text("Субъект (Имя\\ для группировки)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Я, Степанов, Санек…") }
            )

            Spacer(Modifier.height(16.dp))

            // Toggles row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = t.isStem,
                    onClick = { viewModel.toggleStem() },
                    label = { Text("Стволовая") },
                    leadingIcon = if (t.isStem) {{ Icon(Icons.Filled.Repeat, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null
                )
                FilterChip(
                    selected = t.isUrgent,
                    onClick = { viewModel.toggleUrgent() },
                    label = { Text("Срочная") },
                    leadingIcon = if (t.isUrgent) {{ Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = CategoryUrgent) }} else null
                )
            }

            Spacer(Modifier.height(16.dp))

            // Notes
            Text("Заметки / История", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = editNotes,
                onValueChange = { editNotes = it; viewModel.updateNotes(it) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                minLines = 5,
                placeholder = { Text("История револьверного проекта, пометки…") }
            )

            Spacer(Modifier.height(16.dp))

            // Dates with date picker
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DateRow("Создано", df.format(Date(t.createdAt)))
                    DateRow(
                        label = "Запланировано",
                        value = if (t.startDate != null) df.format(Date(t.startDate!!)) else "не указана"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (t.startDate != null) "Изменить дату" else "Назначить дату", style = MaterialTheme.typography.labelSmall)
                        }
                        if (t.startDate != null) {
                            OutlinedButton(
                                onClick = { viewModel.setDate(null) },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Очистить", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (t.completedAt != null) DateRow("Выполнено", df.format(Date(t.completedAt!!)))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category selector
            Text("Переместить в:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "***IN" to TaskCategory.INBOX,
                    "**DAY" to TaskCategory.DAY,
                    "**LATER" to TaskCategory.LATER,
                    "*CONTROL" to TaskCategory.CONTROL,
                    ">>MAYBE" to TaskCategory.MAYBE
                ).forEach { (catName, catEnum) ->
                    FilterChip(
                        selected = t.categoryName == catName,
                        onClick = { viewModel.updateCategory(catName, catEnum) },
                        label = { Text(catName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Project selector
            val projectNames by viewModel.projectNames.collectAsState()
            var projectExpanded by remember { mutableStateOf(false) }

            Text("Проект:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = projectExpanded,
                onExpandedChange = { projectExpanded = it }
            ) {
                OutlinedTextField(
                    value = t.projectId ?: "Без проекта",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = projectExpanded,
                    onDismissRequest = { projectExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Без проекта") },
                        onClick = {
                            viewModel.updateProject(null)
                            projectExpanded = false
                        }
                    )
                    projectNames.forEach { proj ->
                        DropdownMenuItem(
                            text = { Text(proj) },
                            onClick = {
                                viewModel.updateProject(proj)
                                projectExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DateRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
