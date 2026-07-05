package com.mnfarzaneh.taskflow.domain.usecase

import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class ChainWithTasks(
    val chain: Chain,
    val tasks: List<Task>
)

class GetChainWithTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(chainId: Long): Flow<ChainWithTasks?> {
        val chainFlow = kotlinx.coroutines.flow.flow {
            emit(repository.getChainById(chainId))
        }
        val tasksFlow = repository.getTasksByChain(chainId)

        return combine(chainFlow, tasksFlow) { chain, tasks ->
            chain?.let { ChainWithTasks(it, tasks.sortedBy { t -> t.order }) }
        }
    }
}