package aimotive.simulation.location

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

object LocationRequestProvider {

    operator fun invoke(): LocationRequest =
        LocationRequest.Builder(1_000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(1f)
            .setMinUpdateIntervalMillis(500)
            .build()
}
