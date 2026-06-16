package com.kozvits.kislogtd.data.db

import com.kozvits.kislogtd.data.db.entity.NoteEntity
import com.kozvits.kislogtd.domain.model.Note

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
    taskId = taskId,
    categoryName = categoryName
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
    taskId = taskId,
    categoryName = categoryName
)
