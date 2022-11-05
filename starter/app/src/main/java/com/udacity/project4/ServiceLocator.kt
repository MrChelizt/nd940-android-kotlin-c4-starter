package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.DefaultReminderDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository

object ServiceLocator {

    private val lock = Any()
    private var database: RemindersDatabase? = null

    @Volatile
    var reminderDataSource: ReminderDataSource? = null
        @VisibleForTesting set

    fun provideReminderDataSource(context: Context): ReminderDataSource {
        synchronized(this) {
            return reminderDataSource ?: createReminderDataSource(context)
        }
    }

    private fun createReminderDataSource(context: Context): ReminderDataSource {
        val newRepo = DefaultReminderDataSource(createRemindersLocalRepository(context))
        reminderDataSource = newRepo
        return newRepo
    }


    private fun createRemindersLocalRepository(context: Context): ReminderDataSource {
        val database = database ?: createDataBase(context)
        return RemindersLocalRepository(database.reminderDao())
    }

    private fun createDataBase(context: Context): RemindersDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "Reminders.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            reminderDataSource = null
        }
    }

}