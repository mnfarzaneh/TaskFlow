package com.mnfarzaneh.taskflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, ChainEntity::class],
    version  = 3,          // ← از 1 به 2
    exportSchema = false
)
abstract class TaskFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun chainDao(): ChainDao
}