package com.kozvits.kislogtd.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.kislogtd.sync.SyncManager
import com.kozvits.kislogtd.sync.SyncStateRepository
import com.kozvits.kislogtd.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val syncStateRepository: SyncStateRepository,
    private val syncManager: SyncManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        // Load sync settings
        viewModelScope.launch {
            syncStateRepository.settings.collect { settings ->
                _state.update {
                    it.copy(
                        isSyncEnabled = settings.isEnabled,
                        autoSync = settings.autoSync,
                        syncIntervalHours = settings.syncIntervalHours,
                        lastSyncTime = if (settings.lastSyncTimestamp > 0L) {
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
                                .format(Date(settings.lastSyncTimestamp))
                        } else ""
                    )
                }
                // Verify connection
                if (settings.isEnabled && settings.accessToken.isNotBlank()) {
                    verifyConnection()
                }
            }
        }

        // Load app version
        try {
            val pkg = application.packageManager.getPackageInfo(
                application.packageName, 0
            )
            _state.update {
                it.copy(appVersion = pkg.versionName ?: "1.0.0")
            }
        } catch (_: Exception) {
            // ignore
        }
    }

    private suspend fun verifyConnection() {
        val result = syncManager.getDropboxUserInfo()
        result.onSuccess { user ->
            _state.update { it.copy(userEmail = user.email) }
        }.onFailure { e ->
            addLog("Ошибка подключения: ${e.message}")
        }
    }

    fun toggleTheme() {
        _state.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun setManualToken(token: String) {
        viewModelScope.launch {
            syncStateRepository.setAccessToken(token, "")
            addLog("Токен сохранён, проверка соединения...")
            // Give DataStore time to propagate
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
                it.copy(
                    isSyncEnabled = false,
                    userEmail = "",
                    lastSyncTime = ""
                )
            }
            addLog("Dropbox отключён")
        }
    }

    fun uploadToDropbox() {
        viewModelScope.launch {
            _state.update { it.copy(syncInProgress = true) }
            addLog("Выгрузка...")
            val result = syncManager.uploadToDropbox()
            if (result.success) {
                addLog("✓ ${result.message}")
            } else {
                addLog("✗ ${result.message}")
            }
            _state.update { it.copy(syncInProgress = false) }
        }
    }

    fun downloadFromDropbox() {
        viewModelScope.launch {
            _state.update { it.copy(syncInProgress = true) }
            addLog("Загрузка...")
            val result = syncManager.downloadFromDropbox()
            if (result.success) {
                addLog("✓ ${result.message}")
            } else {
                addLog("✗ ${result.message}")
            }
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

    fun showDeleteConfirm() {
        addLog("Удаление данных — не реализовано")
    }

    private fun addLog(msg: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale("ru"))
            .format(Date())
        _state.update {
            it.copy(syncLog = "$timestamp: $msg\n${it.syncLog}")
        }
    }
}
