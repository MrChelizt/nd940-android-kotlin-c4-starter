package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.FakeAndroidTestDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.TestUtils.withRecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.*


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun initRepository() {
        repository = FakeAndroidTestDataSource()
        ServiceLocator.reminderDataSource = repository
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    repository
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    repository
                )
            }
            single { FakeAndroidTestDataSource(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }

    @After
    fun cleanupDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        // GIVEN
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        // WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())

        //THEN
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun displayReminders() = runBlockingTest {
        //GIVEN
        val data1 = ReminderDTO("Reminder1", "Desc1", "Loc1", 1.0, 1.1, "111")
        repository.saveReminder(data1)

        val data2 = ReminderDTO("Reminder2", "Desc2", "Loc2", 2.0, 2.2, "222")
        repository.saveReminder(data2)

        //WHEN
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))


        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(0, R.id.title)).check(
            matches(withText(data1.title)))
        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(0, R.id.description)).check(
            matches(withText(data1.description)))
        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(0, R.id.location)).check(
            matches(withText(data1.location)))

        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(1, R.id.title)).check(
            matches(withText(data2.title)))
        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(1, R.id.description)).check(
            matches(withText(data2.description)))
        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(1, R.id.location)).check(
            matches(withText(data2.location)))

    }

    @Test
    fun displayReminders_noData() = runBlockingTest {
        //WHEN
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

    }


}