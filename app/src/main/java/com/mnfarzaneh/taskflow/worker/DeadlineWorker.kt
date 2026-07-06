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
class DeadlineWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID        = "taskflow_deadlines"
        const val KEY_TASK_ID       = "task_id"
        const val KEY_TASK_TITLE    = "task_title"
        const val KEY_CHAIN_TITLE   = "chain_title"    // ← اضافه شد
    }

    override suspend fun doWork(): Result {
        val taskId     = inputData.getLong(KEY_TASK_ID, -1L)
        val taskTitle  = inputData.getString(KEY_TASK_TITLE)  ?: return Result.failure()
        val chainTitle = inputData.getString(KEY_CHAIN_TITLE) ?: ""
        android.util.Log.d("DEADLINE_DEBUG", "taskTitle=$taskTitle, chainTitle=$chainTitle, allKeys=${inputData.keyValueMap.keys}")

        if (taskId == -1L) return Result.failure()

        showDeadlineNotification(taskId, taskTitle, chainTitle)
        return Result.success()
    }

    private fun showDeadlineNotification(taskId: Long, taskTitle: String, chainTitle: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "ددلاین وظایف",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "هشدار ددلاین برای وظایف TaskFlow"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ ددلاین نزدیکه!")
            .setContentText("«$taskTitle» از زنجیره «$chainTitle» به زودی منقضی میشه")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("«$taskTitle» از زنجیره «$chainTitle» به زودی منقضی میشه")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(taskId.toInt() + 10000, notification)
    }
}