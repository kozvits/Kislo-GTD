package com.kozvits.kislogtd.presentation.settings

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kozvits.kislogtd.data.repository.TaskRepository
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskPriority
import com.kozvits.kislogtd.domain.model.TaskStatus
import com.kozvits.kislogtd.sync.SyncManager
import com.kozvits.kislogtd.sync.SyncStateRepository
import com.kozvits.kislogtd.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val exportResult: String? = null
)

data class ExportData(
    val version: Int = 1,
    val exportedAt: String = "",
    val source: String = "kislogtd",
    val tasks: List<ExportTask> = emptyList()
)

data class ExportTask(
    val id: String,
    val title: String,
    val category: String,
    val categoryName: String?,
    val status: String,
    val subjectPrefix: String?,
    val createdAt: Long,
    val startDate: Long?,
    val completedAt: Long?,
    val priority: String,
    val isStem: Boolean,
    val isUrgent: Boolean,
    val notes: String,
    val projectId: String?,
    val sortOrder: Int,
    val contextCategory: String?
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val syncStateRepository: SyncStateRepository,
    private val syncManager: SyncManager,
    private val taskRepository: TaskRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

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
        addLog("Удаление данных — не реализовано")
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
                val tasks = taskRepository.getAllTasksOnce()
                val exportTasks = tasks.map { task ->
                    ExportTask(
                        id = task.id,
                        title = task.title,
                        category = task.category.name,
                        categoryName = task.categoryName,
                        status = task.status.name,
                        subjectPrefix = task.subjectPrefix,
                        createdAt = task.createdAt,
                        startDate = task.startDate,
                        completedAt = task.completedAt,
                        priority = task.priority.name,
                        isStem = task.isStem,
                        isUrgent = task.isUrgent,
                        notes = task.notes,
                        projectId = task.projectId,
                        sortOrder = task.sortOrder,
                        contextCategory = task.contextCategory
                    )
                }
                val exportData = ExportData(
                    version = 1,
                    exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        .format(Date()),
                    source = "kislogtd",
                    tasks = exportTasks
                )
                val json = gson.toJson(exportData)

                val ctx = getApplication<Application>()
                var savedPath: String? = null

                withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Android 10+ — save via MediaStore.Downloads (shows in Downloads app)
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
                        // Android 8-9 — save to app-specific external Downloads (no permissions needed)
                        val downloadsDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        if (downloadsDir != null) {
                            val file = File(downloadsDir, "kislogtd_export.json")
                            file.writeText(json, Charsets.UTF_8)
                            savedPath = file.absolutePath
                        }
                    }
                }

                if (savedPath != null) {
                    addLog("✓ Экспорт завершён: $savedPath (${tasks.size} задач)")
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

                val exportData = gson.fromJson(json, ExportData::class.java)
                if (exportData.tasks.isEmpty()) {
                    addLog("✗ Файл не содержит задач")
                    return@launch
                }

                var imported = 0
                for (et in exportData.tasks) {
                    val category = try { TaskCategory.valueOf(et.category) } catch (_: Exception) { TaskCategory.INBOX }
                    val status = try { TaskStatus.valueOf(et.status) } catch (_: Exception) { TaskStatus.ACTIVE }
                    val priority = try { TaskPriority.valueOf(et.priority) } catch (_: Exception) { TaskPriority.NORMAL }
                    val task = Task(
                        id = et.id,
                        title = et.title,
                        category = category,
                        categoryName = et.categoryName,
                        status = status,
                        subjectPrefix = et.subjectPrefix,
                        createdAt = et.createdAt,
                        startDate = et.startDate,
                        completedAt = et.completedAt,
                        priority = priority,
                        isStem = et.isStem,
                        isUrgent = et.isUrgent,
                        notes = et.notes,
                        projectId = et.projectId,
                        sortOrder = et.sortOrder,
                        contextCategory = et.contextCategory
                    )
                    taskRepository.upsertTask(task)
                    imported++
                }
                addLog("✓ Импорт завершён: $imported задач")
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
