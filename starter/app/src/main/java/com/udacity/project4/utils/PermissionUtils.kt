package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private val runningQOrLater =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

private val runningMOrLater =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

private const val FINE_LOCATION_PERMISSION_INDEX = 0
private const val COARSE_LOCATION_PERMISSION_INDEX = 1
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 0
private const val REQUEST_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

fun Fragment.hasMapLocationPermissions(): Boolean {
    val hasFineLocationPermission = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return hasFineLocationPermission || hasCoarseLocationPermission
}

fun Fragment.shouldShowAdditionalInfo(): Boolean {
    if (!runningMOrLater) return false
    return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
}

@TargetApi(Build.VERSION_CODES.Q)
fun Fragment.hasBackgroundPermissions(): Boolean {
    if (runningQOrLater) {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    return true
}

fun Fragment.requestMapLocationPermissions() {
    var permissionsArray = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    requestPermissions(
        permissionsArray,
        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    )
}

fun Fragment.requestBackgroundPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACKGROUND_PERMISSION_RESULT_CODE
        )
    }
}

fun isBackgroundPermissionResult(requestCode: Int): Boolean {
    return requestCode == REQUEST_BACKGROUND_PERMISSION_RESULT_CODE
}

fun areLocationPermissionsGranted(grantResults: IntArray): Boolean {
    return !(grantResults.isEmpty() ||
            (grantResults[FINE_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED &&
                    grantResults[COARSE_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED))
}

fun isBackgroundPermissionGranted(grantResults: IntArray): Boolean {
    return !(grantResults.isEmpty() ||
            grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
}




