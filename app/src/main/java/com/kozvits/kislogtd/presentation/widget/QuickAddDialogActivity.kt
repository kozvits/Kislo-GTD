package com.kozvits.kislogtd.presentation.widget

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kozvits.kislogtd.data.db.AppDatabase
import com.kozvits.kislogtd.data.db.entity.TaskEntity
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskPriority
import com.kozvits.kislogtd.presentation.theme.KisloGTDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class QuickAddDialogActivity : ComponentActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KisloGTDTheme {
                QuickAddContent(
                    onAdd = { title ->
                        scope.launch {
                            saveTask(title)
                            runOnUiThread {
                                Toast.makeText(
                                    this@QuickAddDialogActivity,
                                    "Задача добавлена в ***IN",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            finish()
                        }
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }

    private suspend fun saveTask(title: String) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(applicationContext)
        val now = System.currentTimeMillis()
        val entity = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            category = TaskCategory.INBOX.name,
            status = "ACTIVE",
            createdAt = now,
            priority = TaskPriority.NORMAL.name,
            isStem = false,
            isUrgent = false,
            notes = "",
            sortOrder = 0
        )
        db.taskDao().upsert(entity)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

@Composable
private fun QuickAddContent(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Быстрое добавление") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Новая задача в ***IN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) onAdd(text.trim())
                },
                enabled = text.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
