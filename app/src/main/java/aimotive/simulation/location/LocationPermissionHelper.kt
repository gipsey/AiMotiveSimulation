package aimotive.simulation.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

class LocationPermissionHelper {

    fun isGranted(context: Context) =
        context.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale(activity: Activity) =
        activity.shouldShowRequestPermissionRationale(PERMISSION)

    companion object {

        internal const val PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }
}
