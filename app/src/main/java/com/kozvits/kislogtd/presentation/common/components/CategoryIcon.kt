package com.kozvits.kislogtd.presentation.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kozvits.kislogtd.presentation.theme.*

@Composable
fun CategoryIcon(
    categoryName: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val (icon, color) = when {
        categoryName.startsWith("***IN") || categoryName == "INBOX" ->
            Icons.Filled.Inbox to CategoryInbox
        categoryName.startsWith("**DAY") || categoryName == "DAY" ->
            Icons.Filled.Today to CategoryDay
        categoryName.startsWith("**LATER") || categoryName == "LATER" ->
            Icons.Filled.Schedule to CategoryLater
        categoryName.startsWith("*CONTROL") || categoryName == "CONTROL" ->
            Icons.Filled.Visibility to CategoryControl
        categoryName.startsWith(">>MAYBE") || categoryName == "MAYBE" ->
            Icons.Filled.AutoAwesome to CategoryMaybe
        categoryName.startsWith("ЯЯ-") ->
            Icons.Filled.FolderOff to Color(0xFF7F8C8D)
        categoryName == categoryName.uppercase() && categoryName.length > 2 ->
            Icons.Filled.Workspaces to CategoryProject
        categoryName.startsWith("\\\\\\") ->
            Icons.Filled.FilterList to MaterialTheme.colorScheme.tertiary
        else ->
            Icons.Filled.Task to MaterialTheme.colorScheme.primary
    }
    Icon(
        imageVector = icon,
        contentDescription = categoryName,
        modifier = modifier,
        tint = if (tint == MaterialTheme.colorScheme.onSurfaceVariant) color else tint
    )
}

@Composable
fun categoryToColor(categoryName: String): Color = when {
    categoryName.startsWith("***IN") || categoryName == "INBOX" -> CategoryInbox
    categoryName.startsWith("**DAY") || categoryName == "DAY" -> CategoryDay
    categoryName.startsWith("**LATER") || categoryName == "LATER" -> CategoryLater
    categoryName.startsWith("*CONTROL") || categoryName == "CONTROL" -> CategoryControl
    categoryName.startsWith(">>MAYBE") || categoryName == "MAYBE" -> CategoryMaybe
    categoryName == categoryName.uppercase() && categoryName.length > 2 -> CategoryProject
    else -> MaterialTheme.colorScheme.primary
}
