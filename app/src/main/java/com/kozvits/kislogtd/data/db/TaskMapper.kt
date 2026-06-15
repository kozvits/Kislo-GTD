package com.kozvits.kislogtd.data.db

import com.kozvits.kislogtd.data.db.entity.TaskEntity
import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskPriority
import com.kozvits.kislogtd.domain.model.TaskStatus

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    category = try {
        TaskCategory.valueOf(category)
    } catch (_: IllegalArgumentException) {
        TaskCategory.INBOX
    },
    status = try {
        TaskStatus.valueOf(status)
    } catch (_: IllegalArgumentException) {
        TaskStatus.ACTIVE
    },
    subjectPrefix = subjectPrefix,
    createdAt = createdAt,
    startDate = startDate,
    completedAt = completedAt,
    priority = try {
        TaskPriority.valueOf(priority)
    } catch (_: IllegalArgumentException) {
        TaskPriority.NORMAL
    },
    isStem = isStem,
    isUrgent = isUrgent,
    notes = notes,
    projectId = projectId,
    sortOrder = sortOrder,
    categoryName = categoryName,
    contextCategory = contextCategory
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    category = category.name,
    status = status.name,
    subjectPrefix = subjectPrefix,
    createdAt = createdAt,
    startDate = startDate,
    completedAt = completedAt,
    priority = priority.name,
    isStem = isStem,
    isUrgent = isUrgent,
    notes = notes,
    projectId = projectId,
    sortOrder = sortOrder,
    categoryName = categoryName,
    contextCategory = contextCategory
)
