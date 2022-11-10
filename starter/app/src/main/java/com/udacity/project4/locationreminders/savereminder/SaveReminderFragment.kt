package com.udacity.project4.locationreminders.savereminder

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderData: ReminderDataItem

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminderData = ReminderDataItem(title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(reminderData)) {
                checkPermissionsAndAddGeofenceRequest()
            } else {
                Snackbar.make(
                    requireView(),
                    _viewModel.showSnackBarInt.value!!,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkPermissionsAndAddGeofenceRequest() {
        if (hasMapLocationPermissions()) {
            if (hasBackgroundPermissions()) {
                checkLocationServicesAndAddGeofenceRequest()
            } else {
                checkAndRequestBackgroundPermission()
            }
        } else {
            requestMapLocationPermissions()
        }
    }

    private fun checkLocationServicesAndAddGeofenceRequest() {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        addGeofenceRequest()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.location_required_error,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.location_settings) {
                            startActivity(Intent().apply {
                                action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                            .show()
                    }
                } else {
                    Log.d(TAG, "Current location is null")
                    Log.e(TAG, "Exception: %s", task.exception)
                    Snackbar.make(
                        requireView(),
                        "Cannot get current location", Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
            Snackbar.make(
                requireView(),
                e.message.toString(), Snackbar.LENGTH_INDEFINITE
            ).show()
        }
    }

    private fun addGeofenceRequest() {
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderData)
            }
            addOnFailureListener {
                Snackbar.make(
                    requireView(),
                    getString(R.string.geofences_not_added),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkAndRequestBackgroundPermission() {
        if (shouldShowAdditionalInfo()) {
            Snackbar.make(
                requireView(),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.give_permission) {
                requestBackgroundPermissions()
            }.show()
        } else {
            requestBackgroundPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (isBackgroundPermissionResult(requestCode)) {
            if (isBackgroundPermissionGranted(grantResults)) {
                checkPermissionsAndAddGeofenceRequest()
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
                )
                    .show()
            }
        } else if (areLocationPermissionsGranted(grantResults)) {
            checkPermissionsAndAddGeofenceRequest()
        } else {
            Snackbar.make(
                requireView(),
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        private val TAG = SaveReminderFragment::class.java.simpleName
        internal const val ACTION_GEOFENCE_EVENT =
            "LocationReminder.action.ACTION_GEOFENCE_EVENT"
        private const val GEOFENCE_RADIUS_IN_METERS = 100f

    }
}
