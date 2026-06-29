package com.kozvits.kislogtd.sync

import com.kozvits.kislogtd.data.db.AppDatabase
import com.kozvits.kislogtd.data.db.entity.NoteEntity
import com.kozvits.kislogtd.data.db.entity.TaskEntity
import com.kozvits.kislogtd.data.db.entity.WeeklyStatsEntity
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val database: AppDatabase
) {

    data class ImportResult(
        val success: Boolean,
        val tasksImported: Int = 0,
        val notesImported: Int = 0,
        val statsImported: Int = 0,
        val error: String = ""
    )

    /**
     * Export the entire database to a JSON string.
     */
    suspend fun exportToJson(): String {
        val tasks = database.taskDao().getAllTasksList()
        val notes = database.noteDao().getAllNotesList()
        val stats = database.weeklyStatsDao().getAllStatsList()

        val root = JSONObject()

        val tasksArray = JSONArray()
        for (task in tasks) {
            tasksArray.put(taskEntityToJson(task))
        }
        root.put("tasks", tasksArray)

        val notesArray = JSONArray()
        for (note in notes) {
            notesArray.put(noteEntityToJson(note))
        }
        root.put("notes", notesArray)

        val statsArray = JSONArray()
        for (stat in stats) {
            statsArray.put(weeklyStatsEntityToJson(stat))
        }
        root.put("weekly_stats", statsArray)

        root.put("export_version", 1)
        root.put("exported_at", System.currentTimeMillis())

        return root.toString(2)
    }

    /**
     * Import JSON string into the database (full replace).
     */
    suspend fun importFromJson(jsonString: String): ImportResult {
        return try {
            val root = JSONObject(jsonString)
            val tasks = mutableListOf<TaskEntity>()
            val notes = mutableListOf<NoteEntity>()
            val stats = mutableListOf<WeeklyStatsEntity>()

            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    tasks.add(taskEntityFromJson(tasksArray.getJSONObject(i)))
                }
            }

            if (root.has("notes")) {
                val notesArray = root.getJSONArray("notes")
                for (i in 0 until notesArray.length()) {
                    notes.add(noteEntityFromJson(notesArray.getJSONObject(i)))
                }
            }

            if (root.has("weekly_stats")) {
                val statsArray = root.getJSONArray("weekly_stats")
                for (i in 0 until statsArray.length()) {
                    stats.add(weeklyStatsEntityFromJson(statsArray.getJSONObject(i)))
                }
            }

            database.taskDao().replaceAll(tasks)
            if (notes.isNotEmpty()) {
                database.noteDao().replaceAll(notes)
            } else {
                database.noteDao().deleteAll()
            }
            database.weeklyStatsDao().replaceAll(stats)

            ImportResult(
                success = true,
                tasksImported = tasks.size,
                notesImported = notes.size,
                statsImported = stats.size
            )
        } catch (e: Exception) {
            ImportResult(success = false, error = e.message ?: "Unknown error")
        }
    }
}

// ── JSON serialization helpers ──────────────────────────────────────────

internal fun taskEntityToJson(task: TaskEntity): JSONObject {
    return JSONObject().apply {
        put("id", task.id)
        put("title", task.title)
        put("notes", task.notes)
        put("category", task.category)
        put("categoryName", task.categoryName ?: "")
        put("subjectPrefix", task.subjectPrefix ?: "")
        put("status", task.status)
        put("priority", task.priority)
        put("isStem", task.isStem)
        put("isUrgent", task.isUrgent)
        put("projectId", task.projectId ?: "")
        put("createdAt", task.createdAt)
        put("startDate", task.startDate ?: 0L)
        put("completedAt", task.completedAt ?: 0L)
        put("sortOrder", task.sortOrder)
        put("contextCategory", task.contextCategory ?: "")
    }
}

internal fun taskEntityFromJson(json: JSONObject): TaskEntity {
    return TaskEntity(
        id = json.getString("id"),
        title = json.getString("title"),
        notes = json.optString("notes", ""),
        category = json.getString("category"),
        categoryName = json.optString("categoryName", "").takeIf { it.isNotEmpty() },
        subjectPrefix = json.optString("subjectPrefix", "").takeIf { it.isNotEmpty() },
        status = json.getString("status"),
        priority = json.getString("priority"),
        isStem = json.optBoolean("isStem", false),
        isUrgent = json.optBoolean("isUrgent", false),
        projectId = json.optString("projectId", "").takeIf { it.isNotEmpty() },
        createdAt = json.getLong("createdAt"),
        startDate = json.optLong("startDate", 0L).takeIf { it > 0 },
        completedAt = json.optLong("completedAt", 0L).takeIf { it > 0 },
        sortOrder = json.optInt("sortOrder", 0),
        contextCategory = json.optString("contextCategory", "").takeIf { it.isNotEmpty() }
    )
}

internal fun noteEntityToJson(note: NoteEntity): JSONObject {
    return JSONObject().apply {
        put("id", note.id)
        put("title", note.title)
        put("body", note.body)
        put("createdAt", note.createdAt)
        put("updatedAt", note.updatedAt)
        put("taskId", note.taskId ?: "")
        put("categoryName", note.categoryName ?: "")
        put("reminderDate", note.reminderDate ?: 0L)
    }
}

internal fun noteEntityFromJson(json: JSONObject): NoteEntity {
    return NoteEntity(
        id = json.getString("id"),
        title = json.optString("title", ""),
        body = json.optString("body", ""),
        createdAt = json.getLong("createdAt"),
        updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
        taskId = json.optString("taskId", "").takeIf { it.isNotEmpty() },
        categoryName = json.optString("categoryName", "").takeIf { it.isNotEmpty() },
        reminderDate = json.optLong("reminderDate", 0L).takeIf { it > 0 }
    )
}

internal fun weeklyStatsEntityToJson(stats: WeeklyStatsEntity): JSONObject {
    return JSONObject().apply {
        put("weekStartDate", stats.weekStartDate)
        put("totalCompleted", stats.totalCompleted)
        put("projectCompleted", stats.projectCompleted)
        put("projectPercent", stats.projectPercent)
        put("diaryEntry", stats.diaryEntry)
    }
}

internal fun weeklyStatsEntityFromJson(json: JSONObject): WeeklyStatsEntity {
    return WeeklyStatsEntity(
        weekStartDate = json.getLong("weekStartDate"),
        totalCompleted = json.getInt("totalCompleted"),
        projectCompleted = json.getInt("projectCompleted"),
        projectPercent = json.getDouble("projectPercent").toFloat(),
        diaryEntry = json.optString("diaryEntry", "")
    )
}
