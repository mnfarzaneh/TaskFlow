package com.mnfarzaneh.taskflow.domain.model

data class Reminder(
    val taskId: Long,
    val taskTitle: String,
    val reminderAt: Long,     // timestamp
    val deadlineAt: Long?     // برای نمایش در notification
)