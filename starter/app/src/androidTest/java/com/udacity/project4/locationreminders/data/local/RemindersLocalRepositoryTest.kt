package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun getReminders() = runBlocking {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        //WHEN
        val result: Result<List<ReminderDTO>> = localRepository.getReminders()

        //THEN
        // THEN - Same task is returned.
        assertThat(result is Result.Success<*>, `is`(true))
        result as Result.Success<List<ReminderDTO>>
        val reminders: List<ReminderDTO> = result.data

        assertThat(reminders.size, `is`(2))

        val reminder1 = reminders.firstOrNull { it.id == data1.id }
        assertThat(reminder1, `is`(CoreMatchers.notNullValue()))
        assertReminder(reminder1!!, data1)

        val reminder2 = reminders.firstOrNull { it.id == data2.id }
        assertThat(reminder2, `is`(CoreMatchers.notNullValue()))
        assertReminder(reminder2!!, data2)

    }

    @Test
    fun saveReminder() = runBlocking {
        //GIVEN
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(0))

        val data = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")

        //WHEN
        localRepository.saveReminder(data)

        //THEN
        val reminder = database.reminderDao().getReminderById(data.id)
        assertThat(reminder, `is`(CoreMatchers.notNullValue()))
        assertReminder(reminder!!, data)

    }

    @Test
    fun getReminder() = runBlocking {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        //WHEN
        val result = localRepository.getReminder(data2.id)

        assertThat(result is Result.Success<*>, `is`(true))
        result as Result.Success<ReminderDTO>
        val reminder: ReminderDTO = result.data

        //THEN
        assertThat(reminder, `is`(CoreMatchers.notNullValue()))
        assertReminder(reminder, data2)
    }

    @Test
    fun getReminder_reminderNotFound() = runBlocking {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        val notSavedId = "333"

        //WHEN
        val result = localRepository.getReminder(notSavedId)

        assertThat(result is Result.Error, `is`(true))
        result as Result.Error

        //THEN
        assertThat( result.message, `is`("Reminder not found!"))
        assertThat(result.statusCode,  `is`(nullValue()))
    }


    @Test
    fun deleteAllReminders() = runBlocking {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        var reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(2))

        //WHEN
       localRepository.deleteAllReminders()

        //THEN
        reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(0))
    }

    private fun assertReminder(reminder: ReminderDTO, data: ReminderDTO) {
        assertThat(reminder.title, `is`(data.title))
        assertThat(reminder.description, `is`(data.description))
        assertThat(reminder.location, `is`(data.location))
        assertThat(reminder.latitude, `is`(data.latitude))
        assertThat(reminder.longitude, `is`(data.longitude))
    }

}