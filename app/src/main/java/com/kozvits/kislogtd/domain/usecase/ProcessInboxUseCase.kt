package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory

interface ProcessInboxUseCase {
    operator fun invoke(
        task: Task,
        newTitle: String,
        targetCategory: String = "**DAY"
    ): Task
}

class ProcessInboxUseCaseImpl : ProcessInboxUseCase {

    override operator fun invoke(
        task: Task,
        newTitle: String,
        targetCategory: String
    ): Task {
        require(task.category == TaskCategory.INBOX) {
            "Task must be in INBOX to process, but was ${task.category}"
        }
        require(newTitle.isNotBlank()) { "New title must not be blank" }

        val updatedNotes = buildString {
            if (task.notes.isNotBlank()) {
                append(task.notes)
                append("\n")
            }
            append("--- Original: ")
            append(task.title)
        }

        return task.copy(
            title = newTitle,
            category = TaskCategory.DAY,
            notes = updatedNotes,
            startDate = System.currentTimeMillis()
        )
    }
}
