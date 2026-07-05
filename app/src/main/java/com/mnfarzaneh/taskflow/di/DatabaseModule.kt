package com.mnfarzaneh.taskflow.di

import android.content.Context
import androidx.room.Room
import com.mnfarzaneh.taskflow.data.local.ChainDao
import com.mnfarzaneh.taskflow.data.local.TaskDao
import com.mnfarzaneh.taskflow.data.local.TaskFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskFlowDatabase =
        Room.databaseBuilder(
            context,
            TaskFlowDatabase::class.java,
            "taskflow_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(database: TaskFlowDatabase): TaskDao =
        database.taskDao()

    @Provides
    @Singleton
    fun provideChainDao(database: TaskFlowDatabase): ChainDao =
        database.chainDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // جدول موقت با ForeignKey بساز
            database.execSQL("""
            CREATE TABLE tasks_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                chainId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT 'LOCKED',
                `order` INTEGER NOT NULL,
                deadlineAt INTEGER,
                reminderAt INTEGER,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(chainId) REFERENCES chains(id) ON DELETE CASCADE
            )
        """)
            // داده‌های قدیمی رو کپی کن
            database.execSQL("""
            INSERT INTO tasks_new 
            SELECT id, chainId, title, description, status, `order`, 
                   deadlineAt, reminderAt, createdAt 
            FROM tasks
            WHERE chainId IN (SELECT id FROM chains)
        """)
            // جدول قدیمی رو حذف کن
            database.execSQL("DROP TABLE tasks")
            // جدول جدید رو rename کن
            database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            // index بساز
            database.execSQL("CREATE INDEX index_tasks_chainId ON tasks(chainId)")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN needsRevision INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE tasks ADD COLUMN revisionNote TEXT")
        }
    }
}