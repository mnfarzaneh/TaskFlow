package com.mnfarzaneh.taskflow.domain.model

enum class TaskStatus {
    LOCKED,    // قفل — وظیفه قبلی انجام نشده
    PENDING,   // آزاد — آماده انجام
    IN_PROGRESS, // در حال انجام
    DONE       // انجام شده
}