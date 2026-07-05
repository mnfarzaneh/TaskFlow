package com.mnfarzaneh.taskflow.data.repository

import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    // ── Chain ────────────────────────────────────────────
    fun getAllChains(): Flow<List<Chain>>
    suspend fun getChainById(chainId: Long): Chain?
    suspend fun insertChain(chain: Chain): Long
    suspend fun updateChain(chain: Chain)
    suspend fun deleteChain(chain: Chain)

    // ── Task ─────────────────────────────────────────────
    fun getTasksByChain(chainId: Long): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)

    suspend fun updateReminder(taskId: Long, reminderAt: Long?)
    fun getTasksWithDeadline(): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    suspend fun setRevisionFlag(taskId: Long, needsRevision: Boolean, note: String?)
}