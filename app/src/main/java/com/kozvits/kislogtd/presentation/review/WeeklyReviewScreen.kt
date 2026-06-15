package com.kozvits.kislogtd.presentation.review

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kozvits.kislogtd.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    navController: NavController,
    viewModel: WeeklyReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var diaryText by remember { mutableStateOf("") }
    val df = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    // Sync diaryText with state when loaded
    LaunchedEffect(state.diaryEntry) {
        if (diaryText.isEmpty() && state.diaryEntry.isNotEmpty()) {
            diaryText = state.diaryEntry
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Недельный обзор") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveDiaryEntry(diaryText)
                        viewModel.saveWeeklyStats()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Stats card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CategoryDay.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Статистика недели", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${df.format(Date(state.weekStart))} \u2014 ${df.format(Date(state.weekEnd - 1))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("Выполнено", "${state.totalCompleted}", CategoryDay)
                            StatItem("По проектам", "${state.projectCompleted}", CategoryProject)
                            StatItem(
                                "Процент",
                                "${state.projectPercent.toInt()}%",
                                if (state.projectPercent >= 25) CategoryDay else CategoryLater
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { state.projectPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = CategoryProject,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            if (state.projectPercent in 20f..35f) "\u2713 Норма (25-30%)"
                            else if (state.projectPercent < 20f) "\u26A0 Мало проектной работы"
                            else "\u2713 Много проектной работы",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Diary entry
            item {
                Text("Дневниковый обзор", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("За что ты можешь себя похвалить на этой неделе?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = diaryText,
                    onValueChange = { diaryText = it; viewModel.saveDiaryEntry(it) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    minLines = 3,
                    placeholder = { Text("Напиши пару слов о достижениях\u2026") }
                )
                Spacer(Modifier.height(16.dp))
            }

            // Maybe review
            item {
                Text(">>MAYBE \u2014 Мечты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CategoryMaybe)
                Spacer(Modifier.height(8.dp))
            }
            items(state.maybeItems, key = { it.id }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = CategoryMaybe.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = CategoryMaybe.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(task.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // History
            item {
                Spacer(Modifier.height(16.dp))
                Text("История", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            items(state.history.reversed().take(8)) { stats ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(df.format(Date(stats.weekStartDate)), style = MaterialTheme.typography.bodySmall)
                        Text("${stats.totalCompleted} дел", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text("${stats.projectPercent.toInt()}%", style = MaterialTheme.typography.bodySmall, color = CategoryProject)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
