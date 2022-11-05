package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders() = runBlockingTest {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        //WHEN
        val reminders = database.reminderDao().getReminders()

        //THEN
        assertThat(reminders.size, `is`(2))

        val reminder1 = reminders.firstOrNull { it.id == data1.id }
        assertThat(reminder1, `is`(notNullValue()))
        assertReminder(reminder1!!, data1)

        val reminder2 = reminders.firstOrNull { it.id == data2.id }
        assertThat(reminder2, `is`(notNullValue()))
        assertReminder(reminder2!!, data2)

    }


    @Test
    fun getReminderById() = runBlockingTest {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        //WHEN
        val reminder = database.reminderDao().getReminderById(data2.id)

        //THEN
        assertThat(reminder, `is`(notNullValue()))
        assertReminder(reminder!!, data2)
    }

    @Test
    fun saveReminder() = runBlockingTest {
        //GIVEN
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(0))

        val data = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")

        //WHEN
        database.reminderDao().saveReminder(data)

        //THEN
        val reminder = database.reminderDao().getReminderById(data.id)
        assertThat(reminder, `is`(notNullValue()))
        assertReminder(reminder!!, data)
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        database.reminderDao().saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        database.reminderDao().saveReminder(data2)

        var reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(2))

        //WHEN
        database.reminderDao().deleteAllReminders()

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