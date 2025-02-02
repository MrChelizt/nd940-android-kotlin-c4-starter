package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.FakeAndroidTestDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.TestUtils.withRecyclerView
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { FakeAndroidTestDataSource() as ReminderDataSource }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun saveReminder() = runBlocking {

        //Start up reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //Navigate to save reminder fragment
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(replaceText("TEST TITLE"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("TEST DESC"))

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.save_location)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(0, R.id.title)).check(
            matches(withText("TEST TITLE"))
        )

        onView(
            withRecyclerView(R.id.reminderssRecyclerView).atPositionOnView(
                0,
                R.id.description
            )
        ).check(
            matches(withText("TEST DESC"))
        )

        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(
                            appContext
                        )?.window?.decorView
                    )
                )
            )
        ).check(matches(isDisplayed()))

        activityScenario.close()
    }


    @Test
    fun saveReminder_enterTitleSnackBar() = runBlocking {

        //Start up reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //Navigate to save reminder fragment
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(replaceText(""))
        onView(withId(R.id.reminderDescription)).perform(replaceText("TEST DESC"))

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun saveReminder_enterLocationSnackBar() = runBlocking {

        //Start up reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //Navigate to save reminder fragment
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(replaceText("TEST TITLE"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("TEST DESC"))

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))

        activityScenario.close()
    }

}
