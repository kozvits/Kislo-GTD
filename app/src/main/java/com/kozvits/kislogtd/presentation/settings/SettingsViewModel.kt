package com.kozvits.kislogtd.presentation.settings

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.sync.BackupManager
import com.kozvits.kislogtd.sync.SyncManager
import com.kozvits.kislogtd.sync.SyncStateRepository
import com.kozvits.kislogtd.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val isSyncEnabled: Boolean = false,
    val userEmail: String = "",
    val lastSyncTime: String = "",
    val autoSync: Boolean = true,
    val syncIntervalHours: Int = 24,
    val syncLog: String = "",
    val syncInProgress: Boolean = false,
    val appVersion: String = "1.0.0",
    val retentionDays: Int = 90,
    val deletedRetentionDays: Int = 30,
    val exportResult: String? = null,
    val showDeleteAllConfirm: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val syncStateRepository: SyncStateRepository,
    private val syncManager: SyncManager,
    private val taskRepository: TaskRepository,
    private val backupManager: BackupManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            syncStateRepository.settings.collect { settings ->
                _state.update {
                    val effectiveDark = if (settings.themeUserSet) {
                        settings.isDarkTheme
                    } else {
                        isSystemInDarkMode()
                    }
                    it.copy(
                        isDarkTheme = effectiveDark,
                        isSyncEnabled = settings.isEnabled,
                        autoSync = settings.autoSync,
                        syncIntervalHours = settings.syncIntervalHours,
                        retentionDays = settings.retentionDays,
                        deletedRetentionDays = settings.deletedRetentionDays,
                        lastSyncTime = if (settings.lastSyncTimestamp > 0L) {
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
                                .format(Date(settings.lastSyncTimestamp))
                        } else ""
                    )
                }
                if (settings.isEnabled && settings.accessToken.isNotBlank()) {
                    verifyConnection()
                }
            }
        }

        try {
            val pkg = application.packageManager.getPackageInfo(
                application.packageName, 0
            )
            _state.update {
                it.copy(appVersion = pkg.versionName ?: "1.0.0")
            }
        } catch (_: Exception) {}
    }

    private suspend fun verifyConnection() {
        val result = syncManager.getDropboxUserInfo()
        result.onSuccess { user ->
            _state.update { it.copy(userEmail = user.email) }
        }.onFailure { e ->
            addLog("Ошибка подключения: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    private fun isSystemInDarkMode(): Boolean {
        val mode = getApplication<Application>().resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newValue = !_state.value.isDarkTheme
            syncStateRepository.setDarkTheme(newValue)
            _state.update { it.copy(isDarkTheme = newValue) }
        }
    }

    fun setManualToken(token: String) {
        viewModelScope.launch {
            syncStateRepository.setAccessToken(token, "")
            addLog("Токен сохранён, проверка соединения...")
            kotlinx.coroutines.delay(300)
            verifyConnection()
        }
    }

    fun disconnectDropbox() {
        viewModelScope.launch {
            syncStateRepository.clearAuth()
            val ctx = getApplication<Application>()
            SyncWorker.cancel(ctx)
            _state.update {
                it.copy(isSyncEnabled = false, userEmail = "", lastSyncTime = "")
            }
            addLog("Dropbox отключён")
        }
    }

    fun uploadToDropbox() {
        viewModelScope.launch {
            _state.update { it.copy(syncInProgress = true) }
            addLog("Выгрузка...")
            val result = syncManager.uploadToDropbox()
            if (result.success) addLog("✓ ${result.message}")
            else addLog("✗ ${result.message}")
            _state.update { it.copy(syncInProgress = false) }
        }
    }

    fun downloadFromDropbox() {
        viewModelScope.launch {
            _state.update { it.copy(syncInProgress = true) }
            addLog("Загрузка...")
            val result = syncManager.downloadFromDropbox()
            if (result.success) addLog("✓ ${result.message}")
            else addLog("✗ ${result.message}")
            _state.update { it.copy(syncInProgress = false) }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            val hours = _state.value.syncIntervalHours
            syncStateRepository.setAutoSync(enabled, hours)
            val ctx = getApplication<Application>()
            if (enabled) {
                SyncWorker.schedule(ctx, hours)
                addLog("Автосинхронизация включена (каждые ${hours}ч)")
            } else {
                SyncWorker.cancel(ctx)
                addLog("Автосинхронизация отключена")
            }
        }
    }

    fun setSyncInterval(hours: Int) {
        viewModelScope.launch {
            val clamped = hours.coerceIn(1, 168)
            syncStateRepository.setAutoSync(_state.value.autoSync, clamped)
            if (_state.value.autoSync) {
                val ctx = getApplication<Application>()
                SyncWorker.schedule(ctx, clamped)
            }
        }
    }

    fun setRetentionDays(days: Int) {
        viewModelScope.launch {
            val clamped = days.coerceIn(1, 730)
            syncStateRepository.setRetentionDays(clamped)
            _state.update { it.copy(retentionDays = clamped) }
            addLog("Время хранения выполненных: $clamped дн.")
        }
    }

    fun setDeletedRetentionDays(days: Int) {
        viewModelScope.launch {
            val clamped = days.coerceIn(1, 365)
            syncStateRepository.setDeletedRetentionDays(clamped)
            _state.update { it.copy(deletedRetentionDays = clamped) }
            addLog("Время хранения удалённых: $clamped дн.")
        }
    }

    fun showDeleteConfirm() {
        _state.update { it.copy(showDeleteAllConfirm = true) }
    }

    fun dismissDeleteConfirm() {
        _state.update { it.copy(showDeleteAllConfirm = false) }
    }

    fun confirmDeleteAll() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteAllConfirm = false, syncInProgress = true) }
            try {
                val tasks = taskRepository.getAllTasksOnce()
                for (task in tasks) {
                    taskRepository.deleteTask(task)
                }
                addLog("✓ Все данные удалены (${tasks.size} задач)")
            } catch (e: Exception) {
                addLog("✗ Ошибка удаления: ${e.message}")
            } finally {
                _state.update { it.copy(syncInProgress = false) }
            }
        }
    }

    fun dismissExportResult() {
        _state.update { it.copy(exportResult = null) }
    }

    // ═══════════════════════════════════════════════════════════════
    //  JSON Export — save directly to Downloads/
    // ═══════════════════════════════════════════════════════════════

    fun exportToJson() {
        viewModelScope.launch {
            _state.update { it.copy(syncInProgress = true) }
            try {
                // Use BackupManager for unified export format
                val json = backupManager.exportToJson()

                val ctx = getApplication<Application>()
                var savedPath: String? = null

                withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = android.content.ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, "kislogtd_export.json")
                            put(MediaStore.Downloads.MIME_TYPE, "application/json")
                            put(MediaStore.Downloads.IS_PENDING, 1)
                        }
                        val uri = ctx.contentResolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
                        )
                        if (uri != null) {
                            ctx.contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(json.toByteArray(Charsets.UTF_8))
                                out.flush()
                            }
                            contentValues.clear()
                            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                            ctx.contentResolver.update(uri, contentValues, null, null)
                            savedPath = "Загрузки/kislogtd_export.json"
                        }
                    } else {
                        val downloadsDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        if (downloadsDir != null) {
                            val file = File(downloadsDir, "kislogtd_export.json")
                            file.writeText(json, Charsets.UTF_8)
                            savedPath = file.absolutePath
                        }
                    }
                }

                if (savedPath != null) {
                    addLog("✓ Экспорт завершён: $savedPath")
                    _state.update { it.copy(exportResult = "Файл сохранён: $savedPath") }
                } else {
                    addLog("✗ Не удалось создать файл экспорта")
                }
            } catch (e: Exception) {
                addLog("✗ Ошибка экспорта: ${e.message}")
            } finally {
                _state.update { it.copy(syncInProgress = false) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  JSON Import — parse content from SAF-chosen URI
    // ═══════════════════════════════════════════════════════════════

    fun importFromJsonUri(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(syncInProgress = true) }
            try {
                val ctx = getApplication<Application>()
                val json = withContext(Dispatchers.IO) {
                    ctx.contentResolver.openInputStream(uri)?.use { input ->
                        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
                    } ?: ""
                }
                if (json.isBlank()) {
                    addLog("✗ Файл пуст")
                    return@launch
                }

                val result = backupManager.importFromJson(json)
                if (result.success) {
                    addLog("✓ Импорт завершён: ${result.tasksImported} задач, ${result.notesImported} заметок")
                } else {
                    addLog("✗ Ошибка импорта: ${result.error}")
                }
            } catch (e: Exception) {
                addLog("✗ Ошибка импорта: ${e.message}")
            } finally {
                _state.update { it.copy(syncInProgress = false) }
            }
        }
    }

    private fun addLog(msg: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale("ru")).format(Date())
        _state.update { it.copy(syncLog = "$timestamp: $msg\n${it.syncLog}") }
    }
}
