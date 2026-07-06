package com.mnfarzaneh.taskflow.widget

import android.content.Context
import androidx.room.Room
import com.mnfarzaneh.taskflow.data.local.TaskFlowDatabase
import com.mnfarzaneh.taskflow.domain.model.Chain
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import kotlinx.coroutines.flow.first

data class WidgetChainData(
    val chain: Chain,
    val currentTask: Task?,
    val doneCount: Int,
    val totalCount: Int
)

object WidgetDataProvider {

    suspend fun getChainData(context: Context): List<WidgetChainData> {
        return try {
            val db = Room.databaseBuilder(
                context.applicationContext,
                TaskFlowDatabase::class.java,
                "taskflow_database"
            ).fallbackToDestructiveMigration().build()

            val chains = db.chainDao().getAllChains().first()
            val result = chains.map { chainEntity ->
                val chain      = chainEntity.toDomain()
                val allTasks   = db.taskDao().getTasksByChain(chain.id).first()
                    .map { it.toDomain() }
                val doneCount  = allTasks.count { it.status == TaskStatus.DONE }
                val totalCount = allTasks.size
                val currentTask = allTasks
                    .sortedBy { it.order }
                    .firstOrNull {
                        it.status == TaskStatus.PENDING ||
                                it.status == TaskStatus.IN_PROGRESS
                    }

                WidgetChainData(
                    chain       = chain,
                    currentTask = currentTask,
                    doneCount   = doneCount,
                    totalCount  = totalCount
                )
            }.filter { it.totalCount > 0 && it.doneCount < it.totalCount }
                .sortedBy { it.doneCount.toFloat() / it.totalCount }

            db.close()
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}