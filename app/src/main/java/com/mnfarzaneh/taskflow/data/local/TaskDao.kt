package com.mnfarzaneh.taskflow.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE chainId = :chainId ORDER BY `order` ASC")
    fun getTasksByChain(chainId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE status != 'DONE' AND deadlineAt IS NOT NULL")
    fun getTasksWithDeadline(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET reminderAt = :reminderAt WHERE id = :taskId")
    suspend fun updateReminder(taskId: Long, reminderAt: Long?)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET needsRevision = :needsRevision, revisionNote = :note WHERE id = :taskId")
    suspend fun setRevisionFlag(taskId: Long, needsRevision: Boolean, note: String?)

}