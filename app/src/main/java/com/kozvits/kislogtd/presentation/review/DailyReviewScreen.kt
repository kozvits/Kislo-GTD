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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewScreen(
    navController: NavController,
    viewModel: DailyReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isComplete) {
        // Completion screen
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = CategoryDay)
            Spacer(Modifier.height(16.dp))
            Text("Утренний регламент выполнен!", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("День начинается продуктивно.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) { Text("На главную") }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Утренний регламент") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Закрыть")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Progress bar
            LinearProgressIndicator(
                progress = { state.step.toFloat() / state.totalSteps },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(6.dp),
                color = CategoryDay,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Text(
                "Шаг ${state.step} из ${state.totalSteps}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            when (state.step) {
                1 -> StepInbox(state, viewModel)
                2 -> StepDay(state, viewModel)
                3 -> StepPlaceholder(3, "Пополнить **DAY из **LATER", "Перенеси подходящие дела, если есть место", viewModel)
                4 -> StepControl(state, viewModel)
                5 -> StepProjects(state, viewModel)
            }
        }
    }
}

@Composable
private fun StepInbox(state: DailyReviewState, viewModel: DailyReviewViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Inbox, contentDescription = null, tint = CategoryInbox)
            Spacer(Modifier.width(8.dp))
            Text("Шаг 1: Обработать ***IN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(
            "Преврати каждую запись в элементарное действие",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val inboxItems = state.inboxItems

        if (inboxItems.isEmpty()) {
            Spacer(Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = CategoryDay)
                Spacer(Modifier.height(8.dp))
                Text("***IN пуст! Можно переходить к следующему шагу.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.moveToNextStep() }) { Text("Далее \u2192") }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(inboxItems, key = { it.id }) { item ->
                    InboxProcessCard(item = item, viewModel = viewModel)
                }
            }
            Button(
                onClick = { viewModel.moveToNextStep() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Все обработаны \u2192 Шаг 2") }
        }
    }
}

@Composable
private fun InboxProcessCard(item: Task, viewModel: DailyReviewViewModel) {
    var showProcess by remember { mutableStateOf(false) }
    var processTitle by remember { mutableStateOf(item.title) }
    var targetCategory by remember { mutableStateOf("**DAY") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = processTitle,
                    onValueChange = { processTitle = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Элементарное действие\u2026") }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = targetCategory == "**DAY",
                    onClick = { targetCategory = "**DAY" },
                    label = { Text("**DAY") },
                    leadingIcon = { Icon(Icons.Filled.Today, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = targetCategory == "**LATER",
                    onClick = { targetCategory = "**LATER" },
                    label = { Text("**LATER") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.skipInboxItem(item) }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (processTitle.isNotBlank()) {
                        viewModel.processInboxItem(item, processTitle, targetCategory)
                    }
                }) { Text("Обработать") }
            }
        }
    }
}

@Composable
private fun StepDay(state: DailyReviewState, viewModel: DailyReviewViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Today, contentDescription = null, tint = CategoryDay)
            Spacer(Modifier.width(8.dp))
            Text("Шаг 2: Организовать **DAY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("Проверь и перераспредели текущие действия", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        if (state.dayItems.isEmpty()) {
            Text("**DAY пуст.", modifier = Modifier.padding(vertical = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.dayItems, key = { it.id }) { task ->
                    DayReviewCard(task, viewModel)
                }
            }
        }
        Button(onClick = { viewModel.moveToNextStep() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Далее \u2192")
        }
    }
}

@Composable
private fun DayReviewCard(task: Task, viewModel: DailyReviewViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.displayTitle, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { viewModel.moveTaskToLater(task) }) {
                Icon(Icons.Filled.Schedule, contentDescription = "В **LATER", tint = CategoryLater)
            }
            IconButton(onClick = { viewModel.moveTaskToControl(task) }) {
                Icon(Icons.Filled.Visibility, contentDescription = "В *CONTROL", tint = CategoryControl)
            }
        }
    }
}

@Composable
private fun StepPlaceholder(step: Int, title: String, desc: String, viewModel: DailyReviewViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(desc, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { viewModel.moveToNextStep() }) { Text("Далее \u2192") }
    }
}

@Composable
private fun StepControl(state: DailyReviewState, viewModel: DailyReviewViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Visibility, contentDescription = null, tint = CategoryControl)
            Spacer(Modifier.width(8.dp))
            Text("Шаг 4: Просмотреть *CONTROL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("Отметь выполненные, проверь сроки", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (state.controlItems.isEmpty()) {
            Text("*CONTROL пуст.", modifier = Modifier.padding(vertical = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.controlItems, key = { it.id }) { task ->
                    ControlReviewCard(task, viewModel)
                }
            }
        }
        Button(onClick = { viewModel.moveToNextStep() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Далее \u2192")
        }
    }
}

@Composable
private fun ControlReviewCard(task: Task, viewModel: DailyReviewViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.displayTitle, style = MaterialTheme.typography.bodyMedium)
                if (task.subjectPrefix != null) Text("\u2192 ${task.subjectPrefix}", style = MaterialTheme.typography.labelSmall, color = CategoryControl)
            }
            IconButton(onClick = { viewModel.completeControlTask(task) }) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Выполнено", tint = CategoryDay)
            }
        }
    }
}

@Composable
private fun StepProjects(state: DailyReviewState, viewModel: DailyReviewViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Workspaces, contentDescription = null, tint = CategoryProject)
            Spacer(Modifier.width(8.dp))
            Text("Шаг 5: Текущие проекты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("Быстрый взгляд на проекты", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        // Simplified - just show projects view
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Проекты \u2014 это долгосрочные цели, разбитые на действия.", style = MaterialTheme.typography.bodyMedium)
                Text("Проверь, все ли проекты продвигаются.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.completeReview() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Завершить регламент") }
    }
}
