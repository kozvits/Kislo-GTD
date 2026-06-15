package com.kozvits.kislogtd.domain.model

import java.util.UUID

enum class TaskCategory {
    INBOX,
    DAY,
    LATER,
    CONTROL,
    MAYBE,
    PROJECT,
    CONTEXT
}

enum class TaskStatus {
    ACTIVE,
    COMPLETED,
    DELETED
}

enum class TaskPriority {
    HIGH,
    NORMAL,
    LOW
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: TaskCategory,
    val status: TaskStatus = TaskStatus.ACTIVE,
    val subjectPrefix: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startDate: Long? = null,
    val completedAt: Long? = null,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val isStem: Boolean = false,
    val isUrgent: Boolean = false,
    val notes: String = "",
    val projectId: String? = null,
    val sortOrder: Int = 0,
    val categoryName: String? = null,
    val contextCategory: String? = null
)

val Task.displayTitle: String
    get() = if (subjectPrefix != null) "$subjectPrefix\\$title" else title

val Task.displaySubject: String
    get() = title.substringBefore("\\")
