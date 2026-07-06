package com.mnfarzaneh.taskflow.domain.usecase

import com.mnfarzaneh.taskflow.data.repository.TaskRepository
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus
import com.mnfarzaneh.taskflow.worker.NotificationScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import android.content.Context
import com.mnfarzaneh.taskflow.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext

class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
    private val widgetUpdater: WidgetUpdater,        // ← اضافه شد
    @ApplicationContext private val context: Context  // ← اضافه شد

) {
    suspend operator fun invoke(task: Task) {

        // ۱. وظیفه فعلی رو Done کن
        repository.updateTask(task.copy(status = TaskStatus.DONE))

        // ۲. هشدارهای این وظیفه رو لغو کن
        notificationScheduler.cancelTaskNotifications(task.id)

        // ۳. همه وظایف این زنجیره رو بگیر
        val allTasks = repository.getTasksByChain(task.chainId).first()

        // ۴. وظیفه بعدی رو پیدا کن
        val nextTask = allTasks
            .sortedBy { it.order }
            .firstOrNull { it.order == task.order + 1 }

        // ۵. اگه LOCKED بود آزادش کن و هشدارهاش رو فعال کن
        nextTask?.let {
            if (it.status == TaskStatus.LOCKED) {
                val updatedTask = it.copy(status = TaskStatus.PENDING)
                val chain = repository.getChainById(task.chainId)
                val chainTitle = chain?.title ?: ""
                android.util.Log.d("CHAIN_DEBUG", "chainId=${task.chainId}, chainTitle=$chainTitle")
                repository.updateTask(updatedTask)
                notificationScheduler.scheduleReminder(updatedTask, chainTitle)
                notificationScheduler.scheduleDeadline(updatedTask, chainTitle)
            }
        }
        widgetUpdater.update(context)
    }
}