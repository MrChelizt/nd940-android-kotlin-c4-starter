package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource


    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        dataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        dataSource.setReturnError(false)
        stopKoin()
    }

    @Test
    fun onClear_clearAllFields() {
        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Location"
        saveReminderViewModel.selectedPOI.value = PointOfInterest(LatLng(1.1, 2.2), "name", "name")
        saveReminderViewModel.latitude.value = 1.1
        saveReminderViewModel.longitude.value = 2.2

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.value, `is`(nullValue()))
    }

    @Test
    fun validateAndSaveReminder_successfullySaveReminder() = mainCoroutineRule.runBlockingTest {
        val data = ReminderDataItem("Title", "Description", "Location", 1.1, 2.2)

        saveReminderViewModel.validateAndSaveReminder(data)

        assertReminderIsSaved(data)
        assertThat(saveReminderViewModel.showLoading.value, `is`(false))
        assertThat(saveReminderViewModel.showToast.value, `is`("Reminder Saved !"))
    }

    @Test
    fun validateAndSaveReminder_invalidData() = mainCoroutineRule.runBlockingTest {
        val data = ReminderDataItem("", "", "", 0.0, 0.0)

        saveReminderViewModel.validateAndSaveReminder(data)

        val reminderResult = dataSource.getReminder(data.id)
        assertThat(reminderResult is Result.Error, `is`(true))

        val errorMessage: String? = (reminderResult as Result.Error).message
        assertThat(errorMessage, `is`("Reminder not found"))
    }

    @Test
    fun saveReminder_successfullySaveReminder() = mainCoroutineRule.runBlockingTest {
        val data = ReminderDataItem("Title", "Description", "Location", 1.1, 2.2)

        saveReminderViewModel.saveReminder(data)

        assertReminderIsSaved(data)
        assertThat(saveReminderViewModel.showLoading.value, `is`(false))
        assertThat(saveReminderViewModel.showToast.value, `is`("Reminder Saved !"))
    }


    @Test
    fun validateEnteredData_validData() {
        val data = ReminderDataItem("Title", "", "Location", 0.0, 0.0)

        val response = saveReminderViewModel.validateEnteredData(data)

        assertThat(response, `is`(true))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(nullValue()))
    }


    @Test
    fun validateEnteredData_emptyTitle() {
        val data = ReminderDataItem("", "", "", 0.0, 0.0)

        val response = saveReminderViewModel.validateEnteredData(data)

        assertThat(response, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }


    @Test
    fun validateEnteredData_emptyLocation() {
        val data = ReminderDataItem("Title", "", "", 0.0, 0.0)

        val response = saveReminderViewModel.validateEnteredData(data)

        assertThat(response, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_select_location))
    }

    private suspend fun assertReminderIsSaved(data: ReminderDataItem) {
        val reminderResult = dataSource.getReminder(data.id)
        assertThat(reminderResult is Result.Success<ReminderDTO>, `is`(true))

        val reminder = (reminderResult as Result.Success<ReminderDTO>).data
        assertThat(reminder.title, `is`(data.title))
        assertThat(reminder.description, `is`(data.description))
        assertThat(reminder.location, `is`(data.location))
        assertThat(reminder.latitude, `is`(data.latitude))
        assertThat(reminder.longitude, `is`(data.longitude))
    }



}