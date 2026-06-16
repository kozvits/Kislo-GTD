package com.kozvits.kislogtd.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import com.kozvits.kislogtd.data.db.AppDatabase
import com.kozvits.kislogtd.domain.model.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KisloGtdWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val size = withContext(Dispatchers.IO) { loadDayTasks(context).size }
        val today = SimpleDateFormat("EEEE, dd.MM", Locale("ru")).format(Date())
            .replaceFirstChar { it.uppercase() }

        provideContent {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$today · $size задач из **DAY")
            }
        }
    }

    private suspend fun loadDayTasks(context: Context): List<TaskWidgetItem> {
        val db = AppDatabase.getInstance(context)
        val entities = db.taskDao().getByCategorySync("DAY")
        return entities
            .filter { it.status == TaskStatus.ACTIVE.name }
            .map { TaskWidgetItem(it.id, it.title, it.subjectPrefix, it.isStem, it.isUrgent) }
    }
}

data class TaskWidgetItem(
    val id: String,
    val title: String,
    val subjectPrefix: String?,
    val isStem: Boolean,
    val isUrgent: Boolean
)
class KisloGtdWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KisloGtdWidget()
}
