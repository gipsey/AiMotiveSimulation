package aimotive.simulation.map

import aimotive.simulation.BuildConfig
import aimotive.simulation.R
import aimotive.simulation.model.LatitudeLongitudeItem
import android.content.Context
import androidx.annotation.DrawableRes
import org.maplibre.android.maps.Style

object MapConfiguration {

    const val ZOOM = 16.0

    const val IS_COMPASS_ENABLED = false
    const val GESTURES_ENABLED = false

    val FALLBACK_CAMERA_POSITION = LatitudeLongitudeItem(lat = 47.527554717597354, long = 19.035485994240776)

    val STATIC_LOCATION_DOT_DRAWABLE = LocationDrawable("location_static_dot", R.drawable.location_static_dot)

    val LOCATION_DOT_DRAWABLE = LocationDrawable("location_dot", R.drawable.location_dot)

    fun createStyleBuilder(context: Context) =
        Style.Builder()
            .fromUri(BuildConfig.MAPLIBRE_MAP_STYLE)
            .withImage(
                STATIC_LOCATION_DOT_DRAWABLE.id,
                context.resources.getDrawable(STATIC_LOCATION_DOT_DRAWABLE.drawableResId, null)
            )
            .withImage(
                LOCATION_DOT_DRAWABLE.id,
                context.resources.getDrawable(LOCATION_DOT_DRAWABLE.drawableResId, null)
            )
}

data class LocationDrawable(
    val id: String,
    @DrawableRes val drawableResId: Int,
)
