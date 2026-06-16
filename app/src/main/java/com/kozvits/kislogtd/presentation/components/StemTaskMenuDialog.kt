package com.kozvits.kislogtd.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kozvits.kislogtd.presentation.theme.CategoryStem

/**
 * Toodledo-style popup menu for stem (recurring) tasks.
 * Shows when user taps the checkbox on a stem task.
 */
@Composable
fun StemTaskMenuDialog(
    taskTitle: String,
    onDismiss: () -> Unit,
    onCompleteAndDuplicate: () -> Unit,
    onCompleteOnce: () -> Unit,
    onSkip: () -> Unit,
    onConvertToRegular: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = null,
                    tint = CategoryStem,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Стволовая задача",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    taskTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                StemMenuOption(
                    icon = Icons.Filled.Repeat,
                    text = "Завершить и создать копию",
                    description = "Задача останется в **DAY с новой датой",
                    onClick = {
                        onCompleteAndDuplicate()
                        onDismiss()
                    }
                )
                StemMenuOption(
                    icon = Icons.Filled.CheckCircle,
                    text = "Завершить (без копии)",
                    description = "Завершить это вхождение, ствол отключается",
                    onClick = {
                        onCompleteOnce()
                        onDismiss()
                    }
                )
                StemMenuOption(
                    icon = Icons.Filled.SkipNext,
                    text = "Пропустить",
                    description = "Убрать из **DAY до завтра",
                    onClick = {
                        onSkip()
                        onDismiss()
                    }
                )
                StemMenuOption(
                    icon = Icons.Filled.UnfoldLess,
                    text = "Преобразовать в обычную",
                    description = "Отключить режим стволовой задачи",
                    onClick = {
                        onConvertToRegular()
                        onDismiss()
                    }
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                StemMenuOption(
                    icon = Icons.Filled.Edit,
                    text = "Редактировать",
                    description = "Открыть детальный просмотр задачи",
                    onClick = {
                        onEdit()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun StemMenuOption(
    icon: ImageVector,
    text: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
