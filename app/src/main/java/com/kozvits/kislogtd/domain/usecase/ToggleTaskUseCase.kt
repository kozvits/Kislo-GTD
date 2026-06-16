package com.kozvits.kislogtd.domain.usecase

import com.kozvits.kislogtd.domain.model.Task
import com.kozvits.kislogtd.domain.model.TaskStatus
import javax.inject.Inject

interface ToggleTaskUseCase {
    operator fun invoke(task: Task): Task
}

class ToggleTaskUseCaseImpl @Inject constructor() : ToggleTaskUseCase {

    override operator fun invoke(task: Task): Task {
        return if (task.status == TaskStatus.COMPLETED) {
            task.copy(
                status = TaskStatus.ACTIVE,
                completedAt = null
            )
        } else {
            task.copy(
                status = TaskStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            )
        }
    }
}
