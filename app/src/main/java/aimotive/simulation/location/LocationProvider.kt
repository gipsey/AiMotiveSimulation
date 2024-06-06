package aimotive.simulation.location

import aimotive.simulation.model.LatitudeLongitudeItem
import aimotive.simulation.util.tag
import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class LocationProvider(
    private val client: FusedLocationProviderClient,
) {

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun request(
        onLocationAvailable: (LatitudeLongitudeItem) -> Unit,
        onLostAvailability: () -> Unit,
    ) {
        // remove a potential callback, since a new one is going to be set
        reset()

        client.requestLocationUpdates(
            LocationRequestProvider(),
            LocationCallbackImpl(onLocationAvailable, onLostAvailability)
                .also { callback = it },
            Looper.getMainLooper()
        ).also { task ->
            Log.d(tag(), "request - isSuccessful=${task.isSuccessful} - exception=${task.exception}")
        }
    }

    fun reset() {
        Log.d(tag(), "reset - $callback")
        callback?.let { client.removeLocationUpdates(it) }
    }
}

private class LocationCallbackImpl(
    private val onLocationAvailable: (LatitudeLongitudeItem) -> Unit,
    private val onLostAvailability: () -> Unit,
) : LocationCallback() {

    override fun onLocationResult(locationResult: LocationResult) {
        locationResult.locations.lastOrNull()?.let { location ->
            Log.d(tag(), "onLocationResult - $location")
            onLocationAvailable(LatitudeLongitudeItem(lat = location.latitude, long = location.longitude))
        } ?: Log.w(tag(), "onLocationResult - locations is empty")
    }

    override fun onLocationAvailability(availability: LocationAvailability) {
        Log.d(tag(), "onLocationAvailability - $availability")
        if (!availability.isLocationAvailable) {
            onLostAvailability()
        }
    }
}
