package aimotive.simulation.model

import aimotive.simulation.map.MapConfiguration
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

sealed interface MainLocationState {

    data object Loading : MainLocationState

    data class Success(
        val cameraPosition: CameraPosition,
        val latLng: LatLng,
        val type: Type,
    ) : MainLocationState {

        enum class Type(val locationDrawableId: String) {
            FALLBACK(locationDrawableId = MapConfiguration.STATIC_LOCATION_DOT_DRAWABLE.id),
            ACTUAL(locationDrawableId = MapConfiguration.LOCATION_DOT_DRAWABLE.id),
        }
    }
}
