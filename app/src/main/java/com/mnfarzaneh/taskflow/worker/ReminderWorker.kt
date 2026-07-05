package com.mnfarzaneh.taskflow.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID       = "taskflow_reminders"
        const val KEY_TASK_ID      = "task_id"
        const val KEY_TASK_TITLE   = "task_title"
    }

    override suspend fun doWork(): Result {
        val taskId    = inputData.getLong(KEY_TASK_ID, -1L)
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()

        if (taskId == -1L) return Result.failure()

        showNotification(taskId, taskTitle)
        return Result.success()
    }

    private fun showNotification(taskId: Long, taskTitle: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // ساخت Channel (برای Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "یادآوری وظایف",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "هشدار یادآوری برای وظایف TaskFlow"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("یادآوری وظیفه")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(taskId.toInt(), notification)
    }
}