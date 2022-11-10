package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        val reminders = mutableListOf(
            ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111"),
            ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222"),
            ReminderDTO("Reminder3", "Desc3", "Loc3", 3.0, 3.3, "333")
        )
        dataSource = FakeDataSource(reminders)
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        dataSource.setReturnError(false)
        stopKoin()
    }

    @Test
    fun loadReminders_updateRemindersList() {
        val expected: List<ReminderDataItem> = mutableListOf(
            ReminderDataItem("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111"),
            ReminderDataItem("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222"),
            ReminderDataItem("Reminder3", "Desc3", "Loc3", 3.0, 3.3, "333")
        )

        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.value,  `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.remindersList.value, `is`(expected))
        assertThat(remindersListViewModel.showSnackBar.value,  `is`(nullValue()))
        assertThat(remindersListViewModel.showNoData.value,  `is`(false))
        assertThat(remindersListViewModel.showLoading.value,  `is`(false))

    }

    @Test
    fun loadReminders_throwTestError() {
        dataSource.setReturnError(true)

        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.value,  `is`(true))

        mainCoroutineRule.resumeDispatcher()


        assertThat(remindersListViewModel.remindersList.value.isNullOrEmpty(), `is`(true))
        assertThat(remindersListViewModel.showSnackBar.value,  `is`("Test exception"))
        assertThat(remindersListViewModel.showNoData.value,  `is`(true))
        assertThat(remindersListViewModel.showLoading.value,  `is`(false))
    }

}