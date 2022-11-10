package com.udacity.project4

import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase

object ServiceLocator {

    private val lock = Any()
    private var database: RemindersDatabase? = null

    @Volatile
    var reminderDataSource: ReminderDataSource? = null
        @VisibleForTesting set

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