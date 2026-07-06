package com.mnfarzaneh.taskflow.domain.usecase

import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.worker.NotificationScheduler
import javax.inject.Inject

data class ChainInput(
    val title: String,
    val description: String = "",
    val tasks: List<TaskInput>
)

data class TaskInput(
    val title: String,
    val description: String = "",
    val deadlineAt: Long? = null,
    val reminderAt: Long? = null
)

class CreateChainUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(input: ChainInput) {

        // ۱. زنجیره رو بساز
        val chainId = repository.insertChain(
            Chain(title = input.title, description = input.description)
        )

        // ۲. وظایف رو به ترتیب بساز
        input.tasks.forEachIndexed { index, taskInput ->
            val taskId = repository.insertTask(
                Task(
                    chainId     = chainId,
                    title       = taskInput.title,
                    description = taskInput.description,
                    status      = if (index == 0) TaskStatus.PENDING else TaskStatus.LOCKED,
                    order       = index,
                    deadlineAt  = taskInput.deadlineAt,
                    reminderAt  = taskInput.reminderAt
                )
            )
        }
    }
}