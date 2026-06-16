package com.kozvits.kislogtd.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class QuickDateOption(
    val label: String,
    val getMillis: () -> Long?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToodledoDatePickerDialog(
    initialDateMillis: Long? = null,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var selectedMillis by remember { mutableStateOf(initialDateMillis) }
    var displayedMonthMillis by remember {
        mutableStateOf(
            initialDateMillis ?: Calendar.getInstance().timeInMillis
        )
    }
    val displayedMonth = remember(displayedMonthMillis) {
        Calendar.getInstance().apply { timeInMillis = displayedMonthMillis }
    }

    val quickOptions = remember {
        listOf(
            QuickDateOption("Сегодня") {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            QuickDateOption("Завтра") {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            QuickDateOption("Через неделю") {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            QuickDateOption("Через месяц") {
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        )
    }
    val dfMonth = remember { SimpleDateFormat("LLLL yyyy", Locale("ru")) }
    val dfDayNames = remember { SimpleDateFormat("EEE", Locale("ru")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Выберите дату", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Quick presets — single row of 4
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickOptions.forEach { option ->
                        QuickChip(
                            label = option.label,
                            selected = option.getMillis() == selectedMillis,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMillis = option.getMillis()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = displayedMonthMillis
                            add(Calendar.MONTH, -1)
                        }
                        displayedMonthMillis = cal.timeInMillis
                    }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Пред. месяц")
                    }
                    Text(
                        text = dfMonth.format(Date(displayedMonthMillis)).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = displayedMonthMillis
                            add(Calendar.MONTH, 1)
                        }
                        displayedMonthMillis = cal.timeInMillis
                    }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "След. месяц")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Day-of-week headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    dayNames.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Calendar grid — 7 columns by rows
                val todayCal = remember { Calendar.getInstance() }
                val dayRows = remember(displayedMonth) { chunkDays(getDaysForMonth(displayedMonth)) }
                dayRows.forEach { weekDays ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDays.forEach { (dayMillis, dayOfMonth) ->
                            if (dayOfMonth == 0) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                                val isToday = dayMillis == normalizeDate(todayCal.timeInMillis)
                                val isSelected = selectedMillis != null && dayMillis == normalizeDate(selectedMillis!!)
                                val isWeekend = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                                        cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary)
                                            else Modifier
                                        )
                                        .clickable { selectedMillis = dayMillis },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            isWeekend -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (isToday && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                .align(Alignment.BottomCenter)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected date display
                Spacer(Modifier.height(12.dp))
                if (selectedMillis != null) {
                    val dfSelected = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ru")) }
                    Text(
                        text = dfSelected.format(Date(selectedMillis!!)).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "дата не выбрана",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(selectedMillis)
                onDismiss()
            }) {
                Text("Готово", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun QuickChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 6.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getDaysForMonth(calendar: Calendar): List<Pair<Long, Int>> {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    // Convert to Monday=1 .. Sunday=7
    val offset = (firstDayOfWeek + 5) % 7 // Monday=0
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val result = mutableListOf<Pair<Long, Int>>()

    // Empty cells before first day
    repeat(offset) { result.add(0L to 0) }

    val dayCal = calendar.clone() as Calendar
    for (day in 1..daysInMonth) {
        dayCal.set(Calendar.DAY_OF_MONTH, day)
        val millis = normalizeDate(dayCal.timeInMillis)
        result.add(millis to day)
    }

    return result
}

private fun normalizeDate(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun chunkDays(days: List<Pair<Long, Int>>): List<List<Pair<Long, Int>>> {
    val rows = mutableListOf<MutableList<Pair<Long, Int>>>()
    var currentRow = mutableListOf<Pair<Long, Int>>()
    for (day in days) {
        currentRow.add(day)
        if (currentRow.size == 7) {
            rows.add(currentRow)
            currentRow = mutableListOf()
        }
    }
    // Pad the last row to 7 cells so all rows have equal spacing
    if (currentRow.isNotEmpty()) {
        while (currentRow.size < 7) {
            currentRow.add(0L to 0)
        }
        rows.add(currentRow)
    }
    return rows
}
