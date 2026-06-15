package com.kozvits.kislogtd.sync

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class DropboxUser(
    val accountId: String,
    val email: String,
    val displayName: String
)

@Singleton
class DropboxApiClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val API_DOMAIN = "https://api.dropboxapi.com"
        private const val CONTENT_DOMAIN = "https://content.dropboxapi.com"
        private const val APP_FOLDER = "/KisloGTD"
        private const val BACKUP_FILE = "$APP_FOLDER/kislogtd_backup.json"
    }

    private fun authHeader(token: String) = "Bearer $token"

    /**
     * Upload JSON backup to Dropbox.
     */
    fun uploadBackup(token: String, jsonData: String): Result<Unit> {
        return try {
            val arg = JSONObject().apply {
                put("path", BACKUP_FILE)
                put("mode", "overwrite")
                put("autorename", false)
                put("mute", true)
            }

            val body = jsonData.toRequestBody("application/octet-stream".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$CONTENT_DOMAIN/2/files/upload")
                .header("Authorization", authHeader(token))
                .header("Dropbox-API-Arg", arg.toString())
                .header("Content-Type", "application/octet-stream")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Upload failed: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download JSON backup from Dropbox.
     */
    fun downloadBackup(token: String): Result<String> {
        return try {
            val arg = JSONObject().apply {
                put("path", BACKUP_FILE)
            }

            val body = arg.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$CONTENT_DOMAIN/2/files/download")
                .header("Authorization", authHeader(token))
                .header("Dropbox-API-Arg", arg.toString())
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(responseBody)
            } else {
                // If file not found, return empty backup object so we can start fresh
                if (responseBody.contains("not_found") || responseBody.contains("path")) {
                    Result.success("{}")
                } else {
                    Result.failure(IOException("Download failed: $responseBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current Dropbox user info to verify the token works.
     */
    fun getUserInfo(token: String): Result<DropboxUser> {
        return try {
            val body = "null".toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$API_DOMAIN/2/users/get_current_account")
                .header("Authorization", authHeader(token))
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                Result.success(
                    DropboxUser(
                        accountId = json.getString("account_id"),
                        email = json.getString("email"),
                        displayName = json.getString("display_name")
                    )
                )
            } else {
                Result.failure(IOException("Get user failed: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
