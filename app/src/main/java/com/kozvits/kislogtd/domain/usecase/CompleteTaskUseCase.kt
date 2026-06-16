package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskStatus
import javax.inject.Inject

interface CompleteTaskUseCase {
    operator fun invoke(task: Task): Task
}

class CompleteTaskUseCaseImpl @Inject constructor() : CompleteTaskUseCase {

    override operator fun invoke(task: Task): Task {
        val now = System.currentTimeMillis()

        return if (task.isStem) {
            // Stem (repeating) tasks: don't mark as done,
            // just record the completion time and push startDate forward
            val nextStartDate = (task.startDate ?: now) + 24 * 60 * 60 * 1000L
            task.copy(
                status = TaskStatus.ACTIVE,
                startDate = nextStartDate,
                completedAt = now
            )
        } else {
            task.copy(
                status = TaskStatus.COMPLETED,
                completedAt = now
            )
        }
    }
}
