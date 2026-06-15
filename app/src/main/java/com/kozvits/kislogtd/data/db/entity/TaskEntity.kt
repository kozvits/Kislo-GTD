package com.kozvits.kislogtd.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "subject_prefix")
    val subjectPrefix: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "start_date")
    val startDate: Long? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "priority")
    val priority: String,

    @ColumnInfo(name = "is_stem")
    val isStem: Boolean = false,

    @ColumnInfo(name = "is_urgent")
    val isUrgent: Boolean = false,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "project_id")
    val projectId: String? = null,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "category_name")
    val categoryName: String? = null,

    @ColumnInfo(name = "context_category")
    val contextCategory: String? = null
)
