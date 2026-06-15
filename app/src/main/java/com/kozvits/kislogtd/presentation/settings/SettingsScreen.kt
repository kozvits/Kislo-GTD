package com.kozvits.kislogtd.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kozvits.kislogtd.presentation.theme.CategoryDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSyncLog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }

    // Token input dialog
    if (showTokenDialog) {
        var tokenInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            title = { Text("Ручной ввод токена") },
            text = {
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Access Token") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Вставьте токен из Dropbox App Console") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tokenInput.isNotBlank()) {
                            viewModel.setManualToken(tokenInput.trim())
                            showTokenDialog = false
                        }
                    }
                ) {
                    Text("Подключить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // ── Appearance Section ─────────────────────────────────────
            item {
                SectionHeader("Внешний вид")
                Spacer(Modifier.height(8.dp))
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Тема", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Светлая / Тёмная (скоро)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme() },
                            enabled = false // Coming soon
                        )
                    }
                }
            }

            // ── Sync Section ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Синхронизация с Dropbox")
                Spacer(Modifier.height(8.dp))
            }

            // Sync status card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isSyncEnabled) CategoryDay.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isSyncEnabled) Icons.Filled.CloudDone
                                else Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = if (state.isSyncEnabled) CategoryDay
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (state.isSyncEnabled) "Dropbox подключён"
                                    else "Dropbox не подключён",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.userEmail.isNotEmpty()) state.userEmail
                                    else if (state.isSyncEnabled) "Подключено"
                                    else "Создайте резервную копию в облаке",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (state.lastSyncTime.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Последняя синхронизация: ${state.lastSyncTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Connect / Disconnect button
            item {
                if (state.isSyncEnabled) {
                    Button(
                        onClick = { viewModel.disconnectDropbox() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.LinkOff, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Отключить Dropbox")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showTokenDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Подключить Dropbox")
                    }
                }
            }

            // Manual token hint
            if (!state.isSyncEnabled) {
                item {
                    Text(
                        "Нужен токен доступа Dropbox. Получите его в " +
                            "Dropbox App Console (developers.dropbox.com) " +
                            "и вставьте выше.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }

            // Sync actions
            if (state.isSyncEnabled) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.uploadToDropbox() },
                            modifier = Modifier.weight(1f),
                            enabled = !state.syncInProgress
                        ) {
                            Icon(
                                Icons.Filled.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Выгрузить")
                        }
                        OutlinedButton(
                            onClick = { viewModel.downloadFromDropbox() },
                            modifier = Modifier.weight(1f),
                            enabled = !state.syncInProgress
                        ) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Загрузить")
                        }
                    }
                }
            }

            // Auto-sync settings
            if (state.isSyncEnabled) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Автосинхронизация",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Автоматически выгружать данные каждые ${state.syncIntervalHours} ч",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.autoSync,
                                    onCheckedChange = { viewModel.setAutoSync(it) }
                                )
                            }
                            if (state.autoSync) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Интервал: ${state.syncIntervalHours} ч",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = state.syncIntervalHours.toFloat(),
                                    onValueChange = {
                                        viewModel.setSyncInterval(it.toInt())
                                    },
                                    valueRange = 1f..168f,
                                    steps = 166,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "1ч",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "7 дней",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sync status log
            item {
                Spacer(Modifier.height(8.dp))
                if (state.syncLog.isNotEmpty()) {
                    TextButton(onClick = { showSyncLog = !showSyncLog }) {
                        Text(
                            if (showSyncLog) "Скрыть лог" else "Показать лог синхронизации"
                        )
                    }
                    if (showSyncLog) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    .copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                state.syncLog,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // ── About Section ──────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("О приложении")
                Spacer(Modifier.height(8.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Успеватель",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Версия ${state.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "По методике Василия Кислого «Успеватель для интеллигентов»",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Danger Zone ────────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Данные")
                Spacer(Modifier.height(8.dp))
            }
            item {
                OutlinedButton(
                    onClick = { /* local export - placeholder */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Экспортировать в JSON (локально)")
                }
            }
            item {
                TextButton(
                    onClick = { viewModel.showDeleteConfirm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        "Удалить все данные",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
