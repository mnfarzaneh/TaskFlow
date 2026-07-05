package com.mnfarzaneh.taskflow.widget

import android.content.Context
import com.mnfarzaneh.taskflow.data.local.TaskFlowDatabase
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import kotlinx.coroutines.flow.first
import androidx.room.Room

object WidgetDataProvider {

    suspend fun getPendingTasks(context: Context): List<Task> {
        return try {
            val db = Room.databaseBuilder(
                context.applicationContext,
                TaskFlowDatabase::class.java,
                "taskflow_database"
            ).build()

            val tasks = db.taskDao()
                .getTasksWithDeadline()
                .first()
                .map { it.toDomain() }
                .filter {
                    it.status == TaskStatus.PENDING ||
                            it.status == TaskStatus.IN_PROGRESS
                }
                .sortedWith(
                    compareBy(
                        { it.deadlineAt == null },
                        { it.deadlineAt }
                    )
                )

            db.close()
            tasks
        } catch (e: Exception) {
            emptyList()
        }
    }
}