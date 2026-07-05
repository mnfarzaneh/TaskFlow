package com.mnfarzaneh.taskflow.data.repository

import com.mnfarzaneh.taskflow.data.local.ChainDao
import com.mnfarzaneh.taskflow.data.local.ChainEntity
import com.mnfarzaneh.taskflow.data.local.TaskDao
import com.mnfarzaneh.taskflow.data.local.TaskEntity
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val chainDao: ChainDao
) : TaskRepository {

    // ── Chain ────────────────────────────────────────────
    override fun getAllChains(): Flow<List<Chain>> =
        chainDao.getAllChains().map { list -> list.map { it.toDomain() } }

    override suspend fun getChainById(chainId: Long): Chain? =
        chainDao.getChainById(chainId)?.toDomain()

    override suspend fun insertChain(chain: Chain): Long =
        chainDao.insertChain(ChainEntity.fromDomain(chain))

    override suspend fun updateChain(chain: Chain) =
        chainDao.updateChain(ChainEntity.fromDomain(chain))

    override suspend fun deleteChain(chain: Chain) =
        chainDao.deleteChain(ChainEntity.fromDomain(chain))

    // ── Task ─────────────────────────────────────────────
    override fun getTasksByChain(chainId: Long): Flow<List<Task>> =
        taskDao.getTasksByChain(chainId).map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(taskId: Long): Task? =
        taskDao.getTaskById(taskId)?.toDomain()

    override suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(TaskEntity.fromDomain(task))

    override suspend fun updateTask(task: Task) =
        taskDao.updateTask(TaskEntity.fromDomain(task))

    override suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(TaskEntity.fromDomain(task))

    override fun getTasksWithDeadline(): Flow<List<Task>> =
        taskDao.getTasksWithDeadline().map { list -> list.map { it.toDomain() } }

    override suspend fun updateReminder(taskId: Long, reminderAt: Long?) =
        taskDao.updateReminder(taskId, reminderAt)

    override fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun setRevisionFlag(taskId: Long, needsRevision: Boolean, note: String?) =
        taskDao.setRevisionFlag(taskId, needsRevision, note)
}