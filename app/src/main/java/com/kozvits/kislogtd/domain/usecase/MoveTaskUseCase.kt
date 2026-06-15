package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskCategory

interface MoveTaskUseCase {
    operator fun invoke(task: Task, targetCategory: String): Task
}

class MoveTaskUseCaseImpl : MoveTaskUseCase {

    override operator fun invoke(task: Task, targetCategory: String): Task {
        val newCategory = when (targetCategory.uppercase().trim()) {
            "***IN", "INBOX" -> TaskCategory.INBOX
            "**DAY", "DAY" -> TaskCategory.DAY
            "**LATER", "LATER" -> TaskCategory.LATER
            "**CONTROL", "CONTROL" -> TaskCategory.CONTROL
            "**MAYBE", "MAYBE" -> TaskCategory.MAYBE
            "**PROJECT", "PROJECT" -> TaskCategory.PROJECT
            "***CONTEXT", "CONTEXT" -> TaskCategory.CONTEXT
            else -> TaskCategory.INBOX
        }

        val now = System.currentTimeMillis()

        return task.copy(
            category = newCategory,
            startDate = when (newCategory) {
                TaskCategory.DAY -> now
                TaskCategory.CONTROL -> task.startDate ?: now
                TaskCategory.PROJECT -> task.startDate ?: now
                else -> task.startDate
            }
        )
    }
}
