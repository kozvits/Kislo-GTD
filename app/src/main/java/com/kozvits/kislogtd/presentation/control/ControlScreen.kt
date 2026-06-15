package com.kozvits.kislogtd.presentation.control

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
import com.kozvits.kislogtd.domain.model.displayTitle
import com.kozvits.kislogtd.presentation.common.components.EmptyState
import com.kozvits.kislogtd.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    navController: NavController,
    viewModel: ControlViewModel = hiltViewModel()
) {
    val tasks by viewModel.controlTasks.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Visibility,
                message = "Нет действий в контроле.\nПоручи кому-нибудь дело и оно появится здесь."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        "*CONTROL — Контроль",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                    Text(
                        "Действия, по которым ожидается результат от других",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }

                items(tasks, key = { it.id }) { task ->
                    ControlTaskCard(
                        task = task,
                        onDone = { viewModel.markDone(task) },
                        onMoveToDay = { viewModel.moveToDay(task) },
                        onSetReminder = { /* TODO: date picker */ },
                        onLongClick = { navController.navigate("task/${task.id}") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlTaskCard(
    task: Task,
    onDone: () -> Unit,
    onMoveToDay: () -> Unit,
    onSetReminder: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .combinedClickable(
                onClick = { /* no-op, buttons handle actions */ },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CategoryControl,
                    modifier = Modifier.size(4.dp, 36.dp)
                ) {}
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.displayTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (task.subjectPrefix != null) {
                        Text(
                            "Ожидается от: ${task.subjectPrefix}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CategoryControl
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Notes / history preview
            if (task.notes.isNotBlank()) {
                Text(
                    task.notes.take(120) + if (task.notes.length > 120) "…" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(
                    onClick = onDone,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CategoryDay.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CategoryDay
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Готово", color = CategoryDay)
                }
                FilledTonalButton(
                    onClick = onMoveToDay,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CategoryControl.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        Icons.Filled.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CategoryControl
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Напомнить", color = CategoryControl)
                }
            }

            // Date reminder
            if (task.startDate != null) {
                val df = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                Text(
                    "Проверить: ${df.format(Date(task.startDate!!))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CategoryControl.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
