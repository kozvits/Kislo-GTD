package com.kozvits.kislogtd.presentation.note

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kozvits.kislogtd.domain.model.Note
import com.kozvits.kislogtd.presentation.components.ToodledoDatePickerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    linkedTaskId: String? = null,
    isNew: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val df = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(noteId, isNew) {
        if (isNew) viewModel.newNote(linkedTaskId)
        else if (noteId != null) viewModel.loadNote(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.note?.id != null && !isNew && uiState.note?.title?.isNotBlank() == true)
                            uiState.note!!.title else "Заметка",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.title.isNotBlank() || uiState.body.isNotBlank()) {
                            viewModel.saveNote { onNavigateBack() }
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (uiState.note?.id != null && !isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.saveNote { onNavigateBack() } },
                        enabled = !uiState.isSaving
                    ) {
                        Text(if (uiState.isSaving) "…" else "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Заголовок заметки") },
                placeholder = { Text("О чём эта заметка?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            // Body editor
            Text("Текст заметки", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            // Formatting toolbar
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatButton(Icons.Filled.FormatBold, "Жирный") {
                    viewModel.setBody(uiState.body + "**жирный текст**")
                }
                FormatButton(Icons.Filled.FormatItalic, "Курсив") {
                    viewModel.setBody(uiState.body + "*курсив*")
                }
                FormatButton(Icons.Filled.FormatListBulleted, "Список") {
                    viewModel.setBody(uiState.body + "\n- пункт 1\n- пункт 2\n- пункт 3")
                }
                FormatButton(Icons.Filled.Checklist, "Чеклист") {
                    viewModel.setBody(uiState.body + "\n[ ] задача 1\n[ ] задача 2\n[ ] задача 3")
                }
                FormatButton(Icons.Filled.HorizontalRule, "Разделитель") {
                    viewModel.setBody(uiState.body + "\n---\n")
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.body,
                onValueChange = { viewModel.setBody(it) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp),
                minLines = 8,
                textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
            )

            Spacer(Modifier.height(24.dp))

            // Date reminder section (Toodledo-style)
            var showDatePicker by remember { mutableStateOf(false) }
            Text("Напоминание", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val reminderDate = uiState.note?.reminderDate
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (reminderDate != null) df.format(Date(reminderDate))
                        else "Назначить дату",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (reminderDate != null) {
                    OutlinedButton(
                        onClick = { viewModel.setReminderDate(null) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Очистить", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (showDatePicker) {
                ToodledoDatePickerDialog(
                    initialDateMillis = uiState.note?.reminderDate,
                    onDateSelected = { millis ->
                        viewModel.setReminderDate(millis)
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Preview section (Toodledo-style preview panel)
            if (uiState.body.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Предпросмотр", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Metadata card (Toodledo-style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Информация", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    uiState.note?.let { note ->
                        if (note.title.isNotBlank()) {
                            MetaRow("Ключевые слова", note.title.split(" ").take(5).joinToString(", "))
                        }
                        if (note.createdAt > 0) {
                            MetaRow("Создано", df.format(Date(note.createdAt)))
                        }
                        if (note.updatedAt > 0) {
                            MetaRow("Изменено", df.format(Date(note.updatedAt)))
                        }
                        if (note.taskId != null) {
                            MetaRow("Связано с задачей", note.taskId.take(12) + "…")
                        }
                        if (note.categoryName != null) {
                            MetaRow("Категория", note.categoryName)
                        }
                        if (note.reminderDate != null) {
                            MetaRow("Напоминание", df.format(Date(note.reminderDate!!)))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить заметку") },
            text = { Text("Заметка будет удалена безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote { onNavigateBack() }
                    showDeleteConfirm = false
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun FormatButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
