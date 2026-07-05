package com.mnfarzaneh.taskflow.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mnfarzaneh.taskflow.domain.model.Task
import com.mnfarzaneh.taskflow.domain.model.TaskStatus

@Entity(
    tableName  = "tasks",
    foreignKeys = [
        ForeignKey(
            entity        = ChainEntity::class,
            parentColumns = ["id"],
            childColumns  = ["chainId"],
            onDelete      = ForeignKey.CASCADE   // ← وقتی زنجیره حذف شد، وظایفش هم حذف بشن
        )
    ],
    indices = [Index("chainId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chainId: Long,
    val title: String,
    val description: String = "",
    val status: String = TaskStatus.LOCKED.name,
    val order: Int,
    val deadlineAt: Long? = null,
    val reminderAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val needsRevision: Boolean = false,        // ← اضافه شد
    val revisionNote: String? = null
) {
    fun toDomain() = Task(
        id          = id,
        chainId     = chainId,
        title       = title,
        description = description,
        status      = TaskStatus.valueOf(status),
        order       = order,
        deadlineAt  = deadlineAt,
        reminderAt  = reminderAt,
        createdAt   = createdAt,
        needsRevision = needsRevision,
        revisionNote = revisionNote
    )

    companion object {
        fun fromDomain(task: Task) = TaskEntity(
            id          = task.id,
            chainId     = task.chainId,
            title       = task.title,
            description = task.description,
            status      = task.status.name,
            order       = task.order,
            deadlineAt  = task.deadlineAt,
            reminderAt  = task.reminderAt,
            createdAt   = task.createdAt
        )
    }
}