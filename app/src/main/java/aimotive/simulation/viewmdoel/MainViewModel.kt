package aimotive.simulation.viewmdoel

import aimotive.simulation.R
import aimotive.simulation.domain.PersistLocationUseCase
import aimotive.simulation.domain.PersistStartOfSessionUseCase
import aimotive.simulation.location.LocationProvider
import aimotive.simulation.location.LocationSettingsHelper
import aimotive.simulation.map.MapConfiguration
import aimotive.simulation.model.LatitudeLongitudeItem
import aimotive.simulation.model.MainLocationState
import aimotive.simulation.util.UiDialog
import aimotive.simulation.util.UiDialogAction
import aimotive.simulation.util.UiMessage
import aimotive.simulation.util.UiMessageAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

class MainViewModel(
    private val locationProvider: LocationProvider,
    private val persistStartOfSessionUseCase: PersistStartOfSessionUseCase,
    private val persistLocationUseCase: PersistLocationUseCase,
) : ViewModel() {

    private val _uiDialogFlow = MutableStateFlow<UiDialog?>(null)
    val uiDialogFlow = _uiDialogFlow.asStateFlow()

    private val _uiMessageFlow = MutableSharedFlow<UiMessage>()
    val uiMessageFlow = _uiMessageFlow.asSharedFlow()

    private val _validateLocationPermissionEventFlow = MutableSharedFlow<Unit>()
    val validateLocationPermissionEventFlow = _validateLocationPermissionEventFlow.asSharedFlow()

    private val _requestLocationPermissionEventFlow = MutableSharedFlow<Unit>()
    val requestLocationPermissionEventFlow = _requestLocationPermissionEventFlow.asSharedFlow()

    private val _mainLocationStateFlow = MutableStateFlow<MainLocationState?>(null)
    val mainLocationState = _mainLocationStateFlow.asStateFlow()

    fun onStart() {
        persistStartOfSessionUseCase()
    }

    fun onStop() {
        locationProvider.reset()
    }

    fun onMessageShouldBeShown(message: UiMessage.OfResId) {
        viewModelScope.launch {
            _uiMessageFlow.emit(message)
        }
    }

    fun showLocationPermissionRationale() {
        _uiDialogFlow.value =
            UiDialog.OfResId(
                title = R.string.location_access_rationale_title,
                message = R.string.location_access_rationale,
                onDismiss = { _uiDialogFlow.value = null },
                positiveAction = UiDialogAction(R.string.agree) {
                    viewModelScope.launch { _requestLocationPermissionEventFlow.emit(Unit) }
                },
                negativeAction = UiDialogAction(R.string.cancel) { _uiDialogFlow.value = null },
            )
    }

    fun onLocationPermissionGrantedAndSettingsAvailable() {
        if (_mainLocationStateFlow.value == null) {
            _mainLocationStateFlow.value = MainLocationState.Loading
        }

        locationProvider
            .request(
                onLocationAvailable = { latitudeLongitudeItem ->
                    val latLng = latitudeLongitudeItem.map()

                    _mainLocationStateFlow.value =
                        MainLocationState.Success(
                            cameraPosition = createCameraPositionBy(latLng),
                            latLng = latLng,
                            type = MainLocationState.Success.Type.ACTUAL
                        )

                    persistLocationUseCase(latitudeLongitudeItem)
                },
                onLostAvailability = {
                    locationProvider.reset()
                    viewModelScope.launch {
                        _uiMessageFlow.emit(
                            UiMessage.OfResId(
                                value = R.string.location_connection_lost,
                            )
                        )

                        val latLng =
                            (_mainLocationStateFlow.value as? MainLocationState.Success)?.latLng
                                ?: MapConfiguration.FALLBACK_CAMERA_POSITION.map()
                        _mainLocationStateFlow.value =
                            MainLocationState.Success(
                                cameraPosition = createCameraPositionBy(latLng),
                                latLng = latLng,
                                type = MainLocationState.Success.Type.FALLBACK,
                            )

                        delay(1000)
                        _validateLocationPermissionEventFlow.emit(Unit)
                    }
                }
            )
    }

    fun onLocationPermissionDeniedWithRationaleNeeded() {
        viewModelScope.launch {
            _uiMessageFlow.emit(
                UiMessage.OfResId(
                    R.string.location_access_deny_error,
                    UiMessageAction(R.string.try_again) {
                        viewModelScope.launch {
                            _validateLocationPermissionEventFlow.emit(Unit)
                        }
                    }
                )
            )
        }

        setFallbackLocation()
    }

    fun onLocationPermissionPermanentlyDenied() {
        setFallbackLocation()
    }

    fun onLocationSettingsEnablementDenied() {
        viewModelScope.launch {
            _uiMessageFlow.emit(
                UiMessage.OfResId(
                    R.string.location_settings_deny_error,
                    UiMessageAction(R.string.try_again) {
                        viewModelScope.launch {
                            _validateLocationPermissionEventFlow.emit(Unit)
                        }
                    }
                )
            )
        }

        setFallbackLocation()
    }

    fun onLocationSettingsEnablementError(error: LocationSettingsHelper.Result.Error) {
        viewModelScope.launch {
            _uiMessageFlow.emit(
                UiMessage.OfResId(
                    error.messageResId,
                    UiMessageAction(R.string.try_again) {
                        viewModelScope.launch {
                            _validateLocationPermissionEventFlow.emit(Unit)
                        }
                    })
            )
        }
    }

    private fun createCameraPositionBy(latLng: LatLng) =
        CameraPosition.Builder()
            .target(latLng)
            .zoom(MapConfiguration.ZOOM)
            .build()

    private fun LatitudeLongitudeItem.map() =
        LatLng(latitude = lat, longitude = long)

    private fun setFallbackLocation() {
        val latLng = MapConfiguration.FALLBACK_CAMERA_POSITION.map()

        _mainLocationStateFlow.value =
            MainLocationState.Success(
                cameraPosition = createCameraPositionBy(latLng),
                latLng = latLng,
                type = MainLocationState.Success.Type.FALLBACK,
            )
    }
}
