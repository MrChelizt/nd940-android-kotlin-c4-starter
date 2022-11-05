package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.wrapEspressoIdlingResource

class DefaultReminderDataSource(
    private val remindersLocalRepository: ReminderDataSource
) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        wrapEspressoIdlingResource {
            return remindersLocalRepository.getReminders()
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        wrapEspressoIdlingResource {
            return remindersLocalRepository.saveReminder(reminder)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        wrapEspressoIdlingResource {
            return remindersLocalRepository.getReminder(id)
        }
    }

    override suspend fun deleteAllReminders() {
        wrapEspressoIdlingResource {
            return remindersLocalRepository.deleteAllReminders()
        }
    }

}