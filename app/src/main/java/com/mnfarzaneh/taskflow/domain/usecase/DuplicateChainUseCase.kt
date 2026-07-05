package com.mnfarzaneh.taskflow.domain.usecase

import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DuplicateChainUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(chainId: Long): Long {
        val original = repository.getChainById(chainId) ?: return -1
        val tasks    = repository.getTasksByChain(chainId).first().sortedBy { it.order }

        // زنجیره جدید بساز
        val newChainId = repository.insertChain(
            Chain(
                title       = "${original.title} (کپی)",
                description = original.description
            )
        )

        // وظایف رو کپی کن — همه LOCKED میشن به جز اولی
        tasks.forEachIndexed { index, task ->
            repository.insertTask(
                Task(
                    chainId     = newChainId,
                    title       = task.title,
                    description = task.description,
                    status      = if (index == 0) TaskStatus.PENDING else TaskStatus.LOCKED,
                    order       = index,
                    deadlineAt  = task.deadlineAt,
                    reminderAt  = task.reminderAt
                )
            )
        }

        return newChainId
    }
}