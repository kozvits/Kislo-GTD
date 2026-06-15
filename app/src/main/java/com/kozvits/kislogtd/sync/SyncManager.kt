package com.kozvits.kislogtd.sync

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class SyncResult(
    val success: Boolean,
    val message: String = "",
    val tasksUploaded: Int = 0,
    val tasksDownloaded: Int = 0
)

@Singleton
class SyncManager @Inject constructor(
    private val dropboxApi: DropboxApiClient,
    private val backupManager: BackupManager,
    private val syncStateRepository: SyncStateRepository
) {
    companion object {
        private const val TAG = "SyncManager"
    }

    /**
     * Upload local database to Dropbox.
     */
    suspend fun uploadToDropbox(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val settings = syncStateRepository.settings.first()
            if (!settings.isEnabled || settings.accessToken.isBlank()) {
                return@withContext SyncResult(false, "Dropbox не подключён")
            }

            // 1. Export database to JSON
            val jsonData = backupManager.exportToJson()
            val tasksCount = try {
                JSONObject(jsonData).getJSONArray("tasks").length()
            } catch (_: Exception) { 0 }

            // 2. Upload to Dropbox
            val result = dropboxApi.uploadBackup(settings.accessToken, jsonData)
            return@withContext if (result.isSuccess) {
                syncStateRepository.setLastSyncTimestamp(
                    System.currentTimeMillis(),
                    "upload_ok"
                )
                SyncResult(
                    success = true,
                    message = "Выгружено задач: $tasksCount",
                    tasksUploaded = tasksCount
                )
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error"
                syncStateRepository.setLastSyncTimestamp(
                    System.currentTimeMillis(),
                    "error: $err"
                )
                SyncResult(false, "Ошибка выгрузки: $err")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            SyncResult(false, "Ошибка: ${e.message}")
        }
    }

    /**
     * Download database from Dropbox and replace local data.
     */
    suspend fun downloadFromDropbox(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val settings = syncStateRepository.settings.first()
            if (!settings.isEnabled || settings.accessToken.isBlank()) {
                return@withContext SyncResult(false, "Dropbox не подключён")
            }

            // 1. Download from Dropbox
            val result = dropboxApi.downloadBackup(settings.accessToken)
            if (result.isSuccess) {
                val jsonData = result.getOrThrow()

                // If empty object, nothing to restore
                if (jsonData == "{}" || jsonData.isBlank()) {
                    return@withContext SyncResult(true, "Нет резервной копии в облаке")
                }

                // 2. Import into database
                val importResult = backupManager.importFromJson(jsonData)
                if (importResult.success) {
                    syncStateRepository.setLastSyncTimestamp(
                        System.currentTimeMillis(),
                        "restore_ok"
                    )
                    SyncResult(
                        success = true,
                        message = "Восстановлено задач: ${importResult.tasksImported}",
                        tasksDownloaded = importResult.tasksImported
                    )
                } else {
                    SyncResult(false, "Ошибка импорта: ${importResult.error}")
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error"
                SyncResult(false, "Ошибка загрузки: $err")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            SyncResult(false, "Ошибка: ${e.message}")
        }
    }

    /**
     * Verify Dropbox connection by fetching user info.
     */
    suspend fun getDropboxUserInfo(): Result<DropboxUser> {
        val settings = syncStateRepository.settings.first()
        if (!settings.isEnabled || settings.accessToken.isBlank()) {
            return Result.failure(Exception("Not connected"))
        }
        return dropboxApi.getUserInfo(settings.accessToken)
    }
}
