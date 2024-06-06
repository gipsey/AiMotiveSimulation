package aimotive.simulation.location

import aimotive.simulation.R
import aimotive.simulation.util.tag
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationSettingsHelper {

    suspend fun validateLocationSettings(context: Context): Result =
        suspendCoroutine { continuation ->
            checkLocationSettings(context)
                .addOnFailureListener { exception ->
                    Log.w(tag(), "checkLocationSettings failure", exception)

                    if (exception is ResolvableApiException &&
                        exception.status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED
                    )
                        continuation.resume(Result.ResolutionRequired(exception))
                    else
                        continuation.resume(Result.Error(R.string.location_settings_error))
                }
                .addOnSuccessListener { response ->
                    val states = response.locationSettingsStates

                    Log.d(
                        "checkLocationSettings",
                        "success\n" +
                            "isGpsPresent=${states?.isGpsPresent}\n" +
                            "isGpsUsable=${states?.isGpsUsable}\n" +
                            "isLocationPresent=${states?.isLocationPresent}\n" +
                            "isLocationUsable=${states?.isLocationUsable}"
                    )

                    if (states == null || !states.isGpsPresent || !states.isGpsUsable)
                        continuation.resume(Result.Error(R.string.location_settings_error))
                    else
                        continuation.resume(Result.Available)
                }
                .addOnCanceledListener {
                    Log.d("checkLocationSettings", "canceled")
                }
        }

    private fun checkLocationSettings(context: Context) =
        LocationServices
            .getSettingsClient(context)
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationRequestProvider())
                    .build()
            )

    sealed interface Result {
        data class Error(@StringRes val messageResId: Int) : Result
        data class ResolutionRequired(val resolvableApiException: ResolvableApiException) : Result
        data object Available : Result
    }
}
