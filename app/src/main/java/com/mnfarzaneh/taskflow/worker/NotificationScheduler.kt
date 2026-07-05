package com.mnfarzaneh.taskflow.worker

import android.content.Context
import androidx.work.*
import com.mnfarzaneh.taskflow.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    // ── یادآوری ──────────────────────────────────────────
    fun scheduleReminder(task: Task) {
        val reminderAt = task.reminderAt ?: return
        val delay = reminderAt - System.currentTimeMillis()
        if (delay <= 0) return

        val data = workDataOf(
            ReminderWorker.KEY_TASK_ID    to task.id,
            ReminderWorker.KEY_TASK_TITLE to task.title
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_${task.id}")
            .build()

        workManager.enqueueUniqueWork(
            "reminder_${task.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // ── ددلاین ───────────────────────────────────────────
    fun scheduleDeadline(task: Task) {
        val deadlineAt = task.deadlineAt ?: return
        val delay = deadlineAt - System.currentTimeMillis()
        if (delay <= 0) return

        val data = workDataOf(
            DeadlineWorker.KEY_TASK_ID    to task.id,
            DeadlineWorker.KEY_TASK_TITLE to task.title
        )

        // هشدار یک ساعت قبل از ددلاین
        val warningDelay = (delay - TimeUnit.HOURS.toMillis(1)).coerceAtLeast(0)

        val request = OneTimeWorkRequestBuilder<DeadlineWorker>()
            .setInitialDelay(warningDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("deadline_${task.id}")
            .build()

        workManager.enqueueUniqueWork(
            "deadline_${task.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // ── لغو هشدارها ──────────────────────────────────────
    fun cancelTaskNotifications(taskId: Long) {
        workManager.cancelUniqueWork("reminder_${taskId}")
        workManager.cancelUniqueWork("deadline_${taskId}")
    }
}