package com.kozvits.kislogtd.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.kozvits.kislogtd.presentation.common.components.CategoryTile
import com.kozvits.kislogtd.presentation.common.components.QuickCaptureFab
import com.kozvits.kislogtd.presentation.navigation.Screen
import com.kozvits.kislogtd.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCaptureDialog by remember { mutableStateOf(false) }
    var captureText by remember { mutableStateOf("") }

    // Quick capture dialog
    if (showCaptureDialog) {
        AlertDialog(
            onDismissRequest = { showCaptureDialog = false; captureText = "" },
            title = { Text("Быстрый захват") },
            text = {
                OutlinedTextField(
                    value = captureText,
                    onValueChange = { captureText = it },
                    placeholder = { Text("Что пришло в голову?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (captureText.isNotBlank()) {
                            viewModel.addQuickTask(captureText)
                            captureText = ""
                            showCaptureDialog = false
                        }
                    }
                ) { Text("В ***IN") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCaptureDialog = false; captureText = "" }
                ) { Text("Отмена") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Успеватель",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Text(
                    text = "Василия Кислого для интеллигентов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("EEEE, d MMMM", Locale("ru")).format(Date()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Today's completions section
            if (uiState.todayCompleted > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CategoryDay.copy(alpha = 0.1f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = CategoryDay
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Выполнено сегодня: ${uiState.todayCompleted}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Category tiles
            item {
                CategoryTile(
                    title = "***IN",
                    subtitle = "Входящий поток — захвати всё!",
                    count = uiState.inboxCount,
                    icon = Icons.Filled.Inbox,
                    color = CategoryInbox,
                    onClick = { navController.navigate(Screen.Inbox.route) }
                )
            }
            item {
                CategoryTile(
                    title = "**DAY",
                    subtitle = "Текущие действия на сегодня",
                    count = uiState.dayCount,
                    icon = Icons.Filled.Today,
                    color = CategoryDay,
                    onClick = { navController.navigate(Screen.Day.route) }
                )
            }
            item {
                CategoryTile(
                    title = "*CONTROL",
                    subtitle = "Ожидание ответа от других",
                    count = uiState.controlCount,
                    icon = Icons.Filled.Visibility,
                    color = CategoryControl,
                    onClick = { navController.navigate(Screen.Control.route) }
                )
            }
            item {
                CategoryTile(
                    title = "**LATER",
                    subtitle = "Отложено на неопределённое время",
                    count = uiState.laterCount,
                    icon = Icons.Filled.Schedule,
                    color = CategoryLater,
                    onClick = { navController.navigate(Screen.Later.route) }
                )
            }
            item {
                CategoryTile(
                    title = ">>MAYBE",
                    subtitle = "Мечты и воздушные замки",
                    count = uiState.maybeCount,
                    icon = Icons.Filled.AutoAwesome,
                    color = CategoryMaybe,
                    onClick = { navController.navigate(Screen.Maybe.route) }
                )
            }

            // Navigation section (projects, reviews)
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Меню",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            item {
                CategoryTile(
                    title = "Проекты",
                    subtitle = "Текущие и отложенные проекты",
                    count = uiState.projectCount,
                    icon = Icons.Filled.Workspaces,
                    color = CategoryProject,
                    onClick = { navController.navigate(Screen.ProjectList.route) }
                )
            }
            item {
                CategoryTile(
                    title = "Утренний регламент",
                    subtitle = "Ежедневная обработка входящих",
                    count = 0,
                    icon = Icons.Filled.Assignment,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { navController.navigate(Screen.DailyReview.route) }
                )
            }
            item {
                CategoryTile(
                    title = "Недельный обзор",
                    subtitle = "Статистика и планирование",
                    count = 0,
                    icon = Icons.Filled.Assessment,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { navController.navigate(Screen.WeeklyReview.route) }
                )
            }
            item {
                CategoryTile(
                    title = "Выполненные задачи",
                    subtitle = "Архив завершённых дел",
                    count = uiState.todayCompleted,
                    icon = Icons.Filled.CheckCircle,
                    color = CategoryDay,
                    onClick = { navController.navigate(Screen.CompletedTasks.route) }
                )
            }
            item {
                CategoryTile(
                    title = "Удаленные задачи",
                    subtitle = "Корзина",
                    count = uiState.deletedCount,
                    icon = Icons.Filled.DeleteSweep,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { navController.navigate(Screen.DeletedTasks.route) }
                )
            }
        }

        // FAB
        QuickCaptureFab(
            onTextCapture = { showCaptureDialog = true },
            onVoiceCapture = { /* TODO: voice capture integration */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
