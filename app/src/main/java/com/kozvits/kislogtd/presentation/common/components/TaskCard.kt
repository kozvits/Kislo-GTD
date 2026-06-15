package com.kozvits.kislogtd.presentation.common.components

import com.kozvits.kislogtd.domain.model.displayTitle
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskStatus
import com.kozvits.kislogtd.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val surfaceColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        label = "cardColor"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Urgent indicator (red left border)
            if (task.isUrgent) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CategoryUrgent)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(15.dp))
            }

            // Checkbox
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = if (task.isStem) CategoryStem else CategoryDay,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(8.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.displayTitle,
                    style = if (task.isStem) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Subject prefix chip
                if (task.subjectPrefix != null && task.subjectPrefix != "Я") {
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = task.subjectPrefix!!,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Category indicator
            if (task.categoryName != null && !task.categoryName!!.startsWith("**")) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryToColor(task.categoryName!!).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = task.categoryName!!.take(8),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = categoryToColor(task.categoryName!!)
                    )
                }
            }
        }
    }
}
