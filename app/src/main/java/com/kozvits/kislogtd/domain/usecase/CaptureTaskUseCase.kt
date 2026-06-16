package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory
import com.kozvits.kislogtd.domain.model.TaskStatus
import javax.inject.Inject

interface CaptureTaskUseCase {
    operator fun invoke(
        title: String,
        notes: String = "",
        category: TaskCategory = TaskCategory.INBOX,
        categoryName: String = "***IN"
    ): Task
}

class CaptureTaskUseCaseImpl @Inject constructor() : CaptureTaskUseCase {

    override operator fun invoke(
        title: String,
        notes: String,
        category: TaskCategory,
        categoryName: String
    ): Task {
        require(title.isNotBlank()) { "Task title must not be blank" }

        return Task(
            title = title,
            category = category,
            categoryName = categoryName,
            notes = notes,
            status = TaskStatus.ACTIVE,
            createdAt = System.currentTimeMillis()
        )
    }
}
