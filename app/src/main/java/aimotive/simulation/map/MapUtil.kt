package aimotive.simulation.map

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager

fun MapLibreMap.setUpUiSettings() {
    uiSettings
        .run {
            isCompassEnabled = MapConfiguration.IS_COMPASS_ENABLED
            setAllGesturesEnabled(MapConfiguration.GESTURES_ENABLED)
        }
}

sealed interface StyleSetupState {

    data object NOT_READY : StyleSetupState

    data class READY(
        val symbolManager: SymbolManager,
        val currentLocationSymbol: Symbol,
    ) : StyleSetupState
}
