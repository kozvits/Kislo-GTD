package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory

interface CaptureTaskUseCase {
    operator fun invoke(
        title: String,
        notes: String = "",
        category: String = "***IN"
    ): Task
}

class CaptureTaskUseCaseImpl : CaptureTaskUseCase {

    override operator fun invoke(
        title: String,
        notes: String,
        category: String
    ): Task {
        require(title.isNotBlank()) { "Task title must not be blank" }

        return Task(
            title = title,
            category = TaskCategory.INBOX,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )
    }
}
