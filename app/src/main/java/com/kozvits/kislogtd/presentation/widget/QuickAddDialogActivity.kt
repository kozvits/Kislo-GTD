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
import androidx.lifecycle.lifecycleScope
import com.kozvits.kislogtd.data.db.dao.TaskDao
import com.kozvits.kislogtd.data.db.entity.TaskEntity
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskPriority
import com.kozvits.kislogtd.presentation.theme.KisloGTDTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class QuickAddDialogActivity : ComponentActivity() {

    @Inject
    lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KisloGTDTheme {
                QuickAddContent(
                    onAdd = { title ->
                        lifecycleScope.launch {
                            saveTask(title)
                            Toast.makeText(this@QuickAddDialogActivity, "Задача добавлена в ***IN", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }

    private suspend fun saveTask(title: String) = withContext(Dispatchers.IO) {
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
        taskDao.upsert(entity)
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
