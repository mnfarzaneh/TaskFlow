package com.mnfarzaneh.taskflow.domain.model


data class Task(
    val id: Long = 0,
    val chainId: Long,           // به کدام زنجیره تعلق داره
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.LOCKED,
    val order: Int,              // ترتیب در زنجیره (0, 1, 2, ...)
    val deadlineAt: Long? = null,  // timestamp — null یعنی deadline نداره
    val reminderAt: Long? = null,  // timestamp — زمان هشدار
    val createdAt: Long = System.currentTimeMillis(),
    val needsRevision: Boolean = false,    // ← اضافه شد
    val revisionNote: String? = null
)